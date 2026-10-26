package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.Constants;
import com.example.blog.dto.ArticleDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.Category;
import com.example.blog.entity.Tag;
import com.example.blog.exception.BusinessException;
import com.example.blog.entity.Comment;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.CategoryMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.TagMapper;
import com.example.blog.service.ArticleService;
import com.example.blog.util.PageResult;
import com.example.blog.util.PageUtils;
import com.example.blog.vo.ArticleAdminVO;
import com.example.blog.vo.ArticleBriefVO;
import com.example.blog.vo.ArticleDetailVO;
import com.example.blog.vo.ArticleManageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文章业务层实现
 * <p>
 * 权限规则：写操作（修改/删除/发布/转草稿）仅作者本人或管理员可执行；
 * 可见性规则：已发布文章所有人可见，草稿仅作者本人或管理员可见。
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    /** 标题长度上限（与 article.title VARCHAR(200) 一致） */
    private static final int MAX_TITLE_LENGTH = 200;

    /** 摘要长度上限（与 article.summary VARCHAR(500) 一致） */
    private static final int MAX_SUMMARY_LENGTH = 500;

    /** 自动生成摘要时截取的字符数 */
    private static final int AUTO_SUMMARY_LENGTH = 150;

    /** 封面URL长度上限（与 article.cover VARCHAR(255) 一致） */
    private static final int MAX_COVER_LENGTH = 255;

    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final CategoryMapper categoryMapper;
    private final CommentMapper commentMapper;

    @Override
    public PageResult<ArticleBriefVO> pagePublished(Long pageNum, Long pageSize, String keyword) {
        // 关键词去空白，空串按不搜索处理
        String kw = keyword == null ? null : keyword.trim();
        if (kw != null && kw.isEmpty()) {
            kw = null;
        }
        Page<ArticleBriefVO> page = new Page<>(
                PageUtils.normalizePageNum(pageNum),
                PageUtils.normalizePageSize(pageSize));
        IPage<ArticleBriefVO> result = baseMapper.selectPublishedPage(page, kw);
        return PageUtils.convert(result);
    }

    @Override
    public PageResult<ArticleAdminVO> pageAdminArticles(Long pageNum, Long pageSize, String keyword,
                                                         Integer status, Long categoryId) {
        // 参数合法性校验
        if (status != null && status != Constants.ARTICLE_DRAFT && status != Constants.ARTICLE_PUBLISHED) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "状态参数只能是 0（草稿）或 1（已发布）");
        }
        String kw = keyword == null ? null : keyword.trim();
        if (kw != null && kw.isEmpty()) {
            kw = null;
        }
        Page<ArticleAdminVO> page = new Page<>(
                PageUtils.normalizePageNum(pageNum),
                PageUtils.normalizePageSize(pageSize));
        IPage<ArticleAdminVO> result = baseMapper.selectAdminPage(page, kw, status, categoryId);
        return PageUtils.convert(result);
    }

    @Override
    public ArticleDetailVO getDetail(Long id, Long currentUserId, Integer currentRole) {
        Article article = getExisting(id);

        // 草稿仅作者本人或管理员可见，其余访客统一按不存在处理，避免泄露草稿的存在性
        if (Constants.ARTICLE_DRAFT == article.getStatus() && !isAuthorOrAdmin(article, currentUserId, currentRole)) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "文章不存在或已删除");
        }

        // 浏览量 +1（仅已发布文章；先自增再查询，返回的即最新浏览量）
        baseMapper.incrementViewCount(id);

        return buildDetailVo(id);
    }

    @Override
    public ArticleDetailVO getDetailForEdit(Long id, Long currentUserId, Integer currentRole) {
        Article article = getExisting(id);

        // 编辑回显仅作者本人或管理员可用；其余人统一按不存在处理
        if (!isAuthorOrAdmin(article, currentUserId, currentRole)) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "文章不存在或已删除");
        }

        return buildDetailVo(id);
    }

    /** 组装文章详情 VO，并填充关联标签列表 */
    private ArticleDetailVO buildDetailVo(Long id) {
        ArticleDetailVO vo = baseMapper.selectDetailVo(id);
        // 无标签时返回空列表而非 null，方便前端直接渲染
        List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(id);
        vo.setTags(tagIds == null || tagIds.isEmpty()
                ? new ArrayList<>()
                : tagMapper.selectBatchIds(tagIds));
        return vo;
    }

    @Override
    public PageResult<ArticleManageVO> pageMyArticles(Long userId, Integer status, Long pageNum, Long pageSize) {
        if (status != null && status != Constants.ARTICLE_DRAFT && status != Constants.ARTICLE_PUBLISHED) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "状态参数只能是 0（草稿）或 1（已发布）");
        }

        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<Article>()
                .eq(Article::getUserId, userId)
                .eq(status != null, Article::getStatus, status)
                // 列表页不需要正文，显式指定查询字段，避免一次性拉出大字段
                .select(Article::getId, Article::getTitle, Article::getSummary, Article::getCover,
                        Article::getViewCount, Article::getStatus, Article::getCategoryId,
                        Article::getCreateTime, Article::getUpdateTime)
                .orderByDesc(Article::getCreateTime)
                .orderByDesc(Article::getId);

        Page<Article> page = new Page<>(
                PageUtils.normalizePageNum(pageNum),
                PageUtils.normalizePageSize(pageSize));
        IPage<Article> result = page(page, wrapper);

        List<ArticleManageVO> voList = new ArrayList<>();
        for (Article article : result.getRecords()) {
            ArticleManageVO vo = new ArticleManageVO();
            BeanUtils.copyProperties(article, vo);
            voList.add(vo);
        }

        // 一次性查出涉及到的分类名称并回填，避免逐条查询造成 N+1
        Set<Long> categoryIds = new LinkedHashSet<>();
        for (ArticleManageVO vo : voList) {
            if (vo.getCategoryId() != null) {
                categoryIds.add(vo.getCategoryId());
            }
        }
        if (!categoryIds.isEmpty()) {
            Map<Long, String> categoryNameMap = categoryMapper.selectBatchIds(categoryIds).stream()
                    .collect(Collectors.toMap(Category::getId, Category::getName));
            for (ArticleManageVO vo : voList) {
                vo.setCategoryName(categoryNameMap.get(vo.getCategoryId()));
            }
        }

        return PageUtils.convert(result, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Article addArticle(Long userId, ArticleDTO dto) {
        checkDto(dto);

        Article article = new Article();
        article.setUserId(userId);
        article.setCategoryId(dto.getCategoryId());
        article.setTitle(dto.getTitle().trim());
        article.setContent(dto.getContent());
        article.setCover(trimToNull(dto.getCover()));
        article.setSummary(resolveSummary(dto.getSummary(), dto.getContent()));
        // 新增时不传状态默认存草稿，避免误操作直接对外公开
        article.setStatus(normalizeStatus(dto.getStatus(), Constants.ARTICLE_DRAFT));
        save(article);

        saveTags(article.getId(), dto.getTagIds());
        return article;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Article updateArticle(Long id, Long operatorId, Integer operatorRole, ArticleDTO dto) {
        Article exist = getExisting(id);
        checkPermission(exist, operatorId, operatorRole);
        checkDto(dto);

        exist.setCategoryId(dto.getCategoryId());
        exist.setTitle(dto.getTitle().trim());
        exist.setContent(dto.getContent());
        exist.setCover(trimToNull(dto.getCover()));
        exist.setSummary(resolveSummary(dto.getSummary(), dto.getContent()));
        // 修改时不传状态则保持原状态不变
        exist.setStatus(normalizeStatus(dto.getStatus(), exist.getStatus()));
        updateById(exist);

        // tagIds 为 null 表示不改动标签；非 null（含空列表）则整体重置
        if (dto.getTagIds() != null) {
            saveTags(id, dto.getTagIds());
        }
        return exist;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id, Long operatorId, Integer operatorRole) {
        Article exist = getExisting(id);
        checkPermission(exist, operatorId, operatorRole);

        // 解除标签关联（中间表物理删除）
        articleTagMapper.deleteByArticleId(id);

        // 级联删除该文章下的全部评论（含回复，逻辑删除）
        // 业务规则：删除文章时，同时删除该文章下的全部评论及其回复
        commentMapper.delete(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, id));

        // 最后逻辑删除文章本身
        removeById(id);
    }

    @Override
    public void publishArticle(Long id, Long operatorId, Integer operatorRole) {
        Article exist = getExisting(id);
        checkPermission(exist, operatorId, operatorRole);
        if (Constants.ARTICLE_PUBLISHED == exist.getStatus()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "文章已是发布状态，请勿重复发布");
        }
        exist.setStatus(Constants.ARTICLE_PUBLISHED);
        updateById(exist);
    }

    @Override
    public void unpublishArticle(Long id, Long operatorId, Integer operatorRole) {
        Article exist = getExisting(id);
        checkPermission(exist, operatorId, operatorRole);
        if (Constants.ARTICLE_DRAFT == exist.getStatus()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "文章已是草稿状态");
        }
        exist.setStatus(Constants.ARTICLE_DRAFT);
        updateById(exist);
    }

    /**
     * 查询文章，不存在（含已逻辑删除）时抛出 404
     */
    private Article getExisting(Long id) {
        if (id == null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "文章ID不能为空");
        }
        Article article = getById(id);
        if (article == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "文章不存在或已删除");
        }
        return article;
    }

    /**
     * 校验当前操作人是否为作者本人或管理员，否则抛出 403
     */
    private void checkPermission(Article article, Long operatorId, Integer operatorRole) {
        if (!isAuthorOrAdmin(article, operatorId, operatorRole)) {
            throw new BusinessException(Constants.CODE_FORBIDDEN,
                    "无权限操作该文章（仅作者本人或管理员可操作）");
        }
    }

    /**
     * 判断操作人是否为作者本人或管理员
     */
    private boolean isAuthorOrAdmin(Article article, Long operatorId, Integer operatorRole) {
        boolean isAdmin = operatorRole != null && operatorRole == Constants.ROLE_ADMIN;
        return isAdmin || (operatorId != null && operatorId.equals(article.getUserId()));
    }

    /**
     * 校验文章请求参数（标题、内容为必填；分类与标签若传了则必须存在）
     */
    private void checkDto(ArticleDTO dto) {
        if (dto == null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "请求参数不能为空");
        }
        String title = dto.getTitle() == null ? "" : dto.getTitle().trim();
        if (title.isEmpty()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "文章标题不能为空");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST,
                    "文章标题不能超过 " + MAX_TITLE_LENGTH + " 个字符");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "文章内容不能为空");
        }
        if (dto.getSummary() != null && dto.getSummary().length() > MAX_SUMMARY_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST,
                    "文章摘要不能超过 " + MAX_SUMMARY_LENGTH + " 个字符");
        }
        if (dto.getCover() != null && dto.getCover().length() > MAX_COVER_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST,
                    "封面图URL不能超过 " + MAX_COVER_LENGTH + " 个字符");
        }

        // 分类存在性校验（允许为 null，表示不分类）
        if (dto.getCategoryId() != null && categoryMapper.selectById(dto.getCategoryId()) == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "分类不存在");
        }

        // 标签存在性校验：提前拦截，避免写入中间表后才发现标签不存在
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            Set<Long> distinctIds = new LinkedHashSet<>(dto.getTagIds());
            if (tagMapper.selectBatchIds(distinctIds).size() != distinctIds.size()) {
                throw new BusinessException(Constants.CODE_NOT_FOUND, "部分标签不存在，请检查后重试");
            }
        }
    }

    /**
     * 重置文章与标签的关联（先全部解除，再逐个建立）
     * tagIds 为空列表时相当于清空该文章的全部标签
     */
    private void saveTags(Long articleId, List<Long> tagIds) {
        if (tagIds == null) {
            return;
        }
        articleTagMapper.deleteByArticleId(articleId);
        for (Long tagId : new LinkedHashSet<>(tagIds)) {
            articleTagMapper.insert(articleId, tagId);
        }
    }

    /**
     * 规范化状态值：非法值抛 400；为 null 时返回传入的默认值
     */
    private Integer normalizeStatus(Integer status, Integer defaultValue) {
        if (status == null) {
            return defaultValue;
        }
        if (status != Constants.ARTICLE_DRAFT && status != Constants.ARTICLE_PUBLISHED) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "状态只能是 0（草稿）或 1（发布）");
        }
        return status;
    }

    /**
     * 去空白；空白串转为 null，避免向数据库写入无意义的空串
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 解析文章摘要
     * <p>
     * 显式传入摘要时直接使用；未传（或传空）时由正文自动生成，
     * 保证列表卡片展示的摘要在正文变更后不会一直是旧内容。
     */
    private String resolveSummary(String summary, String content) {
        String explicit = trimToNull(summary);
        if (explicit != null) {
            return explicit.length() > MAX_SUMMARY_LENGTH
                    ? explicit.substring(0, MAX_SUMMARY_LENGTH) : explicit;
        }
        if (content == null) {
            return null;
        }
        String plain = content
                .replaceAll("```[\\s\\S]*?```", " ")
                .replaceAll("[#*`_~>\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (plain.isEmpty()) {
            return null;
        }
        return plain.length() > AUTO_SUMMARY_LENGTH
                ? plain.substring(0, AUTO_SUMMARY_LENGTH) : plain;
    }
}
