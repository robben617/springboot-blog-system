package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.entity.Article;
import com.example.blog.vo.ArticleAdminVO;
import com.example.blog.vo.ArticleBriefVO;
import com.example.blog.vo.ArticleDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文章 Mapper
 * <p>
 * 单表增删改查走 MyBatis-Plus BaseMapper（逻辑删除由 @TableLogic 自动处理）；
 * 跨表查询与浏览量自增使用手写注解 SQL，需显式拼接 deleted = 0。
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 查询文章详情（含作者昵称/头像、分类名称）
     * 标签列表由 Service 层通过 ArticleTagMapper 单独查询后填充
     */
    @Select("SELECT a.id AS id, a.title AS title, a.content AS content, a.cover AS cover, "
            + "a.summary AS summary, a.view_count AS viewCount, a.status AS status, "
            + "a.category_id AS categoryId, c.name AS categoryName, "
            + "a.user_id AS authorId, u.nickname AS authorNickname, u.avatar AS authorAvatar, "
            + "a.create_time AS createTime, a.update_time AS updateTime "
            + "FROM article a "
            + "LEFT JOIN `user` u ON u.id = a.user_id "
            + "LEFT JOIN category c ON c.id = a.category_id "
            + "WHERE a.id = #{id} AND a.deleted = 0")
    ArticleDetailVO selectDetailVo(@Param("id") Long id);

    /**
     * 分页查询已发布文章（公开列表 / 搜索）
     * <p>
     * keyword 为空时查询全部已发布文章；非空时按标题、摘要、正文模糊匹配。
     * 依据需求「关键词搜索：按标题或正文关键词检索文章」，搜索范围覆盖 title / summary / content。
     *
     * @param page    分页对象（由 MyBatis-Plus 分页插件自动改写 SQL）
     * @param keyword 搜索关键词（可为 null）
     */
    @Select("<script>"
            + "SELECT a.id AS id, a.title AS title, a.summary AS summary, a.cover AS cover, "
            + "a.view_count AS viewCount, a.category_id AS categoryId, c.name AS categoryName, "
            + "a.create_time AS createTime, "
            + "a.user_id AS authorId, u.nickname AS authorNickname "
            + "FROM article a LEFT JOIN `user` u ON u.id = a.user_id "
            + "LEFT JOIN category c ON c.id = a.category_id "
            + "WHERE a.deleted = 0 AND a.status = 1 "
            + "<if test='keyword != null and keyword != \"\"'>"
            + "AND (a.title LIKE CONCAT('%', #{keyword}, '%') "
            + "OR a.summary LIKE CONCAT('%', #{keyword}, '%') "
            + "OR a.content LIKE CONCAT('%', #{keyword}, '%')) "
            + "</if>"
            + "ORDER BY a.create_time DESC, a.id DESC"
            + "</script>")
    IPage<ArticleBriefVO> selectPublishedPage(IPage<ArticleBriefVO> page, @Param("keyword") String keyword);

    /**
     * 管理员文章分页列表
     * <p>
     * 支持按关键词（标题/用户名/昵称）、状态、分类筛选，返回全部文章（含草稿、已发布）。
     * 管理员可查看全站文章，用于后台管理。
     *
     * @param page       分页对象
     * @param keyword    关键词（模糊匹配标题、用户名、昵称），可为 null
     * @param status     状态筛选：0-草稿 1-已发布，null 为全部
     * @param categoryId 分类筛选，null 为全部
     */
    @Select("<script>"
            + "SELECT a.id AS id, a.title AS title, a.summary AS summary, a.cover AS cover, "
            + "a.view_count AS viewCount, a.status AS status, "
            + "a.category_id AS categoryId, c.name AS categoryName, "
            + "a.user_id AS authorId, u.username AS authorUsername, u.nickname AS authorNickname, "
            + "a.create_time AS createTime, a.update_time AS updateTime "
            + "FROM article a "
            + "LEFT JOIN `user` u ON u.id = a.user_id "
            + "LEFT JOIN category c ON c.id = a.category_id "
            + "WHERE a.deleted = 0 "
            + "<if test='status != null'>"
            + "AND a.status = #{status} "
            + "</if>"
            + "<if test='categoryId != null'>"
            + "AND a.category_id = #{categoryId} "
            + "</if>"
            + "<if test='keyword != null and keyword != \"\"'>"
            + "AND (a.title LIKE CONCAT('%', #{keyword}, '%') "
            + "OR u.username LIKE CONCAT('%', #{keyword}, '%') "
            + "OR u.nickname LIKE CONCAT('%', #{keyword}, '%')) "
            + "</if>"
            + "ORDER BY a.create_time DESC, a.id DESC"
            + "</script>")
    IPage<ArticleAdminVO> selectAdminPage(IPage<ArticleAdminVO> page,
                                           @Param("keyword") String keyword,
                                           @Param("status") Integer status,
                                           @Param("categoryId") Long categoryId);

    /**
     * 浏览量自增 1（仅对已发布文章生效）
     * 使用 SQL 侧自增，避免并发下「先查后改」覆盖他人的浏览量
     */
    @Update("UPDATE article SET view_count = view_count + 1 "
            + "WHERE id = #{id} AND deleted = 0 AND status = 1")
    int incrementViewCount(@Param("id") Long id);

    /**
     * 统计全站总浏览量（供后台概览使用）
     * 手写 SQL 需显式拼接 deleted = 0；无数据时返回 0 而非 null
     */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM article WHERE deleted = 0")
    long sumViewCount();

    /**
     * 统计指定状态的文章数量（供后台概览使用）
     *
     * @param status 0-草稿 1-已发布
     */
    @Select("SELECT COUNT(*) FROM article WHERE deleted = 0 AND status = #{status}")
    long countByStatus(@Param("status") Integer status);

    /**
     * 统计指定用户的文章数量（含草稿）
     */
    @Select("SELECT COUNT(*) FROM article WHERE deleted = 0 AND user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 统计指定用户某一状态的文章数量
     *
     * @param userId 用户ID
     * @param status 0-草稿 1-已发布
     */
    @Select("SELECT COUNT(*) FROM article WHERE deleted = 0 AND user_id = #{userId} AND status = #{status}")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 统计指定用户文章的累计浏览量
     */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM article WHERE deleted = 0 AND user_id = #{userId}")
    long sumViewCountByUserId(@Param("userId") Long userId);
}
