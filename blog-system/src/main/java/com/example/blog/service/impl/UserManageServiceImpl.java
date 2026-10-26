package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.blog.common.Constants;
import com.example.blog.dto.ChangePasswordDTO;
import com.example.blog.dto.UpdateProfileDTO;
import com.example.blog.entity.Article;
import com.example.blog.entity.Comment;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.ArticleMapper;
import com.example.blog.mapper.ArticleTagMapper;
import com.example.blog.mapper.CommentMapper;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.UserManageService;
import com.example.blog.util.FileUploadUtil;
import com.example.blog.util.PageResult;
import com.example.blog.util.PageUtils;
import com.example.blog.util.PasswordUtil;
import com.example.blog.vo.UserVO;
import com.example.blog.vo.UserStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户资料与用户管理业务层实现
 * <p>
 * 直接复用已有 UserMapper（含逻辑删除与自动填充能力），
 * 不修改任何已有类，避免与原有用户模块代码产生冲突。
 */
@Service
@RequiredArgsConstructor
public class UserManageServiceImpl implements UserManageService {

    /** 昵称长度上限（与 user.nickname VARCHAR(50) 一致） */
    private static final int MAX_NICKNAME_LENGTH = 50;

    /** 邮箱长度上限（与 user.email VARCHAR(100) 一致） */
    private static final int MAX_EMAIL_LENGTH = 100;

    /** 头像URL长度上限（与 user.avatar VARCHAR(255) 一致） */
    private static final int MAX_AVATAR_LENGTH = 255;

    /** 个人简介长度上限（与 user.bio VARCHAR(300) 一致） */
    private static final int MAX_BIO_LENGTH = 300;

    /** 密码最小长度（与《用户与账号规则》一致） */
    private static final int MIN_PASSWORD_LENGTH = 6;

    /** 密码最大长度（与《用户与账号规则》一致） */
    private static final int MAX_PASSWORD_LENGTH = 20;

    /**
     * 管理员重置密码时使用的系统初始密码
     * <p>
     * 符合《用户与账号规则》第 2 条「6~20 位且须同时包含字母和数字」，如需调整改这一处即可。
     */
    private static final String INITIAL_PASSWORD = "abc123456";

    private final UserMapper userMapper;

    private final ArticleMapper articleMapper;

    private final CommentMapper commentMapper;

    private final ArticleTagMapper articleTagMapper;

    /** 文件上传存储根目录（来自 application.yml，与原有上传配置共用） */
    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public UserVO updateProfile(Long userId, UpdateProfileDTO dto) {
        if (userId == null) {
            throw new BusinessException(Constants.CODE_UNAUTHORIZED, "未登录，请先登录");
        }
        if (dto == null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "请求参数不能为空");
        }
        User user = getExistingUser(userId);

        // 昵称：传了就不能为空白，且不得超过长度上限
        if (dto.getNickname() != null) {
            String nickname = dto.getNickname().trim();
            if (nickname.isEmpty()) {
                throw new BusinessException(Constants.CODE_BAD_REQUEST, "昵称不能为空");
            }
            if (nickname.length() > MAX_NICKNAME_LENGTH) {
                throw new BusinessException(Constants.CODE_BAD_REQUEST,
                        "昵称不能超过 " + MAX_NICKNAME_LENGTH + " 个字符");
            }
            user.setNickname(nickname);
        }
        // 邮箱 / 头像 / 简介：传空串表示清空该字段
        if (dto.getEmail() != null) {
            user.setEmail(trimToNull(dto.getEmail(), MAX_EMAIL_LENGTH, "邮箱"));
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(trimToNull(dto.getAvatar(), MAX_AVATAR_LENGTH, "头像URL"));
        }
        if (dto.getBio() != null) {
            user.setBio(trimToNull(dto.getBio(), MAX_BIO_LENGTH, "个人简介"));
        }

        userMapper.updateById(user);
        return toVo(userMapper.selectById(userId));
    }

    @Override
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        if (userId == null) {
            throw new BusinessException(Constants.CODE_UNAUTHORIZED, "未登录，请先登录");
        }
        if (dto == null || isBlank(dto.getOldPassword()) || isBlank(dto.getNewPassword())) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "原密码和新密码不能为空");
        }
        User user = getExistingUser(userId);

        // 1. 校验原密码是否正确
        if (!PasswordUtil.matches(dto.getOldPassword(), user.getSalt(), user.getPassword())) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "原密码错误");
        }

        // 2. 校验新密码格式（6~20位，且须同时包含字母和数字）
        String newPassword = dto.getNewPassword().trim();
        validatePasswordFormat(newPassword);

        // 3. 重新生成盐值并加密（沿用原有 PasswordUtil 的 MD5 + 随机盐方案）
        String salt = PasswordUtil.generateSalt();
        user.setSalt(salt);
        user.setPassword(PasswordUtil.encrypt(newPassword, salt));
        userMapper.updateById(user);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        // 复用已有 FileUploadUtil：头像上限 2MB，仅允许图片格式。
        // 注意：这里必须把配置里的相对路径（./upload）解析成绝对路径再传入。
        // 原因：Spring Boot 3（Servlet 6）下 MultipartFile#transferTo 遇到相对路径时
        // 是按容器的 multipart 临时目录解析的，会抛 IOException 导致「文件保存失败」。
        // 解析基准是 JVM 的工作目录（即项目启动目录），与 ./upload 的原始语义一致。
        String absoluteUploadPath = Paths.get(uploadPath).toAbsolutePath().normalize().toString();
        return FileUploadUtil.uploadAvatar(file, absoluteUploadPath);
    }

    @Override
    public PageResult<UserVO> pageUsers(Long pageNum, Long pageSize, String keyword, Integer status) {
        if (status != null && status != Constants.STATUS_DISABLED && status != Constants.STATUS_ENABLED) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "状态参数只能是 0（禁用）或 1（启用）");
        }
        String kw = keyword == null ? null : keyword.trim();
        if (kw != null && kw.isEmpty()) {
            kw = null;
        }
        final String finalKw = kw;

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(status != null, User::getStatus, status)
                // 关键词同时匹配用户名或昵称
                .and(finalKw != null, w -> w.like(User::getUsername, finalKw)
                        .or()
                        .like(User::getNickname, finalKw))
                .orderByDesc(User::getCreateTime)
                .orderByDesc(User::getId);

        Page<User> page = new Page<>(
                PageUtils.normalizePageNum(pageNum),
                PageUtils.normalizePageSize(pageSize));
        IPage<User> result = userMapper.selectPage(page, wrapper);

        List<UserVO> voList = new ArrayList<>();
        for (User user : result.getRecords()) {
            voList.add(toVo(user));
        }
        return PageUtils.convert(result, voList);
    }

    @Override
    public UserVO updateStatus(Long id, Integer status) {
        if (status == null || (status != Constants.STATUS_DISABLED && status != Constants.STATUS_ENABLED)) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "状态只能是 0（禁用）或 1（启用）");
        }
        User user = getExistingUser(id);
        user.setStatus(status);
        userMapper.updateById(user);
        return toVo(userMapper.selectById(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = getExistingUser(id);

        // 规则：管理员账号不可被删除
        if (Constants.ROLE_ADMIN == user.getRole()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "管理员账号不可被删除");
        }

        // 1. 查出该用户的全部文章（含草稿），并清理其标签关联
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getUserId, id));
        List<Long> articleIds = new ArrayList<>();
        for (Article article : articles) {
            articleIds.add(article.getId());
            // article_tag 中间表没有逻辑删除字段，必须物理删除，避免残留脏关联
            articleTagMapper.deleteByArticleId(article.getId());
        }

        // 2. 逻辑删除该用户发布的全部文章（草稿与已发布一并处理）
        articleMapper.delete(new LambdaQueryWrapper<Article>().eq(Article::getUserId, id));

        // 3. 逻辑删除该用户发布的全部评论
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getUserId, id));

        // 4. 逻辑删除这些文章下他人的评论，避免留下指向已删文章的孤儿评论
        if (!articleIds.isEmpty()) {
            commentMapper.delete(new LambdaQueryWrapper<Comment>().in(Comment::getArticleId, articleIds));
        }

        // 5. 逻辑删除用户本人（User 实体带 @TableLogic）
        userMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long id) {
        User user = getExistingUser(id);
        // 规则：不提供自助密码找回，由管理员重置为系统初始密码
        String salt = PasswordUtil.generateSalt();
        user.setSalt(salt);
        user.setPassword(PasswordUtil.encrypt(INITIAL_PASSWORD, salt));
        userMapper.updateById(user);
    }

    /**
     * 校验密码格式：长度 6~20 位，且须同时包含字母和数字
     */
    private void validatePasswordFormat(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST,
                    "密码长度需为 " + MIN_PASSWORD_LENGTH + "~" + MAX_PASSWORD_LENGTH + " 位");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "密码须同时包含字母和数字");
        }
    }

    /**
     * 查询用户，不存在（含已逻辑删除）时抛出 404
     */
    private User getExistingUser(Long id) {
        if (id == null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "用户ID不能为空");
        }
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(Constants.CODE_NOT_FOUND, "用户不存在或已被删除");
        }
        return user;
    }

    /**
     * 实体转 VO（只拷贝公开字段，密码与盐值不会被带出）
     */
    private UserVO toVo(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 去空白并按长度上限校验；空白串转为 null（表示清空该字段）
     */
    private String trimToNull(String value, int maxLength, String fieldLabel) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > maxLength) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST,
                    fieldLabel + "不能超过 " + maxLength + " 个字符");
        }
        return trimmed;
    }

    /**
     * 判断字符串是否为空白（null 或全空白）
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        UserStatsVO vo = new UserStatsVO();
        vo.setArticleCount(articleMapper.countByUserId(userId));
        vo.setPublishedCount(articleMapper.countByUserIdAndStatus(userId, Constants.ARTICLE_PUBLISHED));
        vo.setDraftCount(articleMapper.countByUserIdAndStatus(userId, Constants.ARTICLE_DRAFT));
        vo.setCommentCount(commentMapper.countByUserId(userId));
        vo.setViewCount(articleMapper.sumViewCountByUserId(userId));
        return vo;
    }
}
