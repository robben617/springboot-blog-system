package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.Constants;
import com.example.blog.dto.CommentDTO;
import com.example.blog.entity.Comment;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.service.CommentService;
import com.example.blog.util.PageResult;
import com.example.blog.util.PageUtils;
import com.example.blog.vo.AdminCommentVO;
import com.example.blog.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论业务层实现
 * <p>
 * 评论采用两级结构：一级评论（parentId 为空）直接挂在文章下，
 * 回复（parentId 指向一级评论）展示在对应评论下方并缩进一层，回复不支持再嵌套。
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    /** 评论内容长度上限（与 comment.content VARCHAR(500) 一致） */
    private static final int MAX_CONTENT_LENGTH = 500;

    @Override
    public List<CommentVO> listByArticle(Long articleId) {
        if (articleId == null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "文章ID不能为空");
        }
        // 一次查询取回全部评论（已 JOIN 评论人信息），再在内存中组装两级树，避免 N+1 查询
        List<CommentVO> flatList = baseMapper.selectVoListByArticleId(articleId);
        return buildTwoLevelTree(flatList);
    }

    @Override
    public CommentVO addComment(Long userId, CommentDTO dto) {
        if (userId == null) {
            throw new BusinessException(Constants.CODE_UNAUTHORIZED, "未登录，请先登录");
        }
        if (dto == null || dto.getArticleId() == null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "文章ID不能为空");
        }

        // 1. 校验评论内容
        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        if (content.isEmpty()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "评论内容不能为空");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST,
                    "评论内容不能超过 " + MAX_CONTENT_LENGTH + " 个字符");
        }

        // 2. 校验文章存在
        if (baseMapper.countArticle(dto.getArticleId()) == 0) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "文章不存在或已删除");
        }

        // 3. 处理父评论，保证永远只有两级
        Long parentId = null;
        if (dto.getParentId() != null) {
            Comment parent = getById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException(Constants.CODE_NOT_FOUND, "要回复的评论不存在或已删除");
            }
            if (!parent.getArticleId().equals(dto.getArticleId())) {
                throw new BusinessException(Constants.CODE_BAD_REQUEST, "要回复的评论不属于该文章");
            }
            // 需求规定「回复不支持继续嵌套回复」：若回复的是一条回复，
            // 则自动归并到它所属的一级评论下，而不是报错，前端体验更友好
            parentId = parent.getParentId() == null ? parent.getId() : parent.getParentId();
        }

        // 4. 保存评论
        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(content);
        save(comment);

        // 5. 回显评论人昵称 / 头像（不再调用用户模块，直接由 SQL JOIN 取回）
        CommentVO vo = baseMapper.selectVoById(comment.getId());
        return vo == null ? new CommentVO() : vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId, Long operatorId, Integer operatorRole) {
        if (operatorId == null) {
            throw new BusinessException(Constants.CODE_UNAUTHORIZED, "未登录，请先登录");
        }
        Comment comment = getById(commentId);
        if (comment == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "评论不存在或已被删除");
        }

        // 权限校验：管理员放行；否则仅文章作者可删除
        boolean isAdmin = operatorRole != null && operatorRole == Constants.ROLE_ADMIN;
        if (!isAdmin) {
            Long articleAuthorId = baseMapper.selectArticleAuthorId(comment.getArticleId());
            if (articleAuthorId == null || !articleAuthorId.equals(operatorId)) {
                throw new BusinessException(Constants.CODE_FORBIDDEN,
                        "无权限删除该评论（仅管理员或文章作者可删除）");
            }
        }

        // 业务规则：删除评论时一并删除其回复（先删子回复，再删自身；两者都用逻辑删除）
        remove(new LambdaQueryWrapper<Comment>().eq(Comment::getParentId, commentId));
        removeById(commentId);
    }

    @Override
    public PageResult<AdminCommentVO> pageAdminComments(Long pageNum, Long pageSize,
                                                         String keyword, Long articleId) {
        String kw = keyword == null ? null : keyword.trim();
        if (kw != null && kw.isEmpty()) {
            kw = null;
        }
        Page<AdminCommentVO> page = new Page<>(
                PageUtils.normalizePageNum(pageNum),
                PageUtils.normalizePageSize(pageSize));
        IPage<AdminCommentVO> result = baseMapper.selectAdminPage(page, kw, articleId);
        return PageUtils.convert(result);
    }

    /**
     * 把扁平评论列表组装成两级树
     * <p>
     * 正常数据下回复的 parentId 一定指向一级评论（写入时已保证）；
     * 这里对历史脏数据做防御：若父评论本身也是回复，则挂到它所属的一级评论下，避免出现三级结构。
     */
    private List<CommentVO> buildTwoLevelTree(List<CommentVO> flatList) {
        Map<Long, CommentVO> idMap = new LinkedHashMap<>();
        for (CommentVO vo : flatList) {
            idMap.put(vo.getId(), vo);
        }

        List<CommentVO> roots = new ArrayList<>();
        for (CommentVO vo : flatList) {
            Long parentId = vo.getParentId();
            if (parentId == null) {
                roots.add(vo);
                continue;
            }
            CommentVO parent = idMap.get(parentId);
            if (parent == null) {
                // 父评论已被删除：作为一级评论展示，避免回复凭空丢失
                roots.add(vo);
            } else if (parent.getParentId() == null) {
                parent.getReplies().add(vo);
            } else {
                CommentVO root = idMap.get(parent.getParentId());
                if (root != null && root.getParentId() == null) {
                    root.getReplies().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }
}
