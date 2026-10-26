package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.entity.Comment;
import com.example.blog.vo.AdminCommentVO;
import com.example.blog.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评论 Mapper
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 统计文章是否存在（未逻辑删除）
     * 用于发表评论前校验文章有效
     */
    @Select("SELECT COUNT(*) FROM article WHERE id = #{articleId} AND deleted = 0")
    long countArticle(@Param("articleId") Long articleId);

    /**
     * 查询文章作者ID
     * 用于「文章作者可删除自己文章下任意评论」的权限判定
     *
     * @return 文章作者ID；文章不存在时返回 null
     */
    @Select("SELECT user_id FROM article WHERE id = #{articleId} AND deleted = 0")
    Long selectArticleAuthorId(@Param("articleId") Long articleId);

    /**
     * 查询某篇文章下全部有效评论（含评论人昵称与头像）
     * <p>
     * 一次 JOIN 查询取回全部数据，在 Service 层组装两级树，
     * 避免按父评论逐条查子回复造成 N+1 查询。
     * <p>
     * 注意：这里是手写 SQL，MyBatis-Plus 的逻辑删除不会自动拼接，
     * 因此显式写了 c.deleted = 0；表名 comment 是 MySQL 关键字，使用反引号包裹。
     */
    @Select("SELECT c.id AS id, c.article_id AS articleId, c.user_id AS userId, "
            + "c.parent_id AS parentId, c.content AS content, c.create_time AS createTime, "
            + "u.nickname AS userNickname, u.avatar AS userAvatar "
            + "FROM `comment` c LEFT JOIN `user` u ON u.id = c.user_id "
            + "WHERE c.article_id = #{articleId} AND c.deleted = 0 "
            + "ORDER BY c.create_time ASC, c.id ASC")
    List<CommentVO> selectVoListByArticleId(@Param("articleId") Long articleId);

    /**
     * 按ID查询单条评论的展示信息（含评论人昵称、头像）
     * 发表评论后直接回显给前端用，避免再去调用户模块查询昵称
     */
    @Select("SELECT c.id AS id, c.article_id AS articleId, c.user_id AS userId, "
            + "c.parent_id AS parentId, c.content AS content, c.create_time AS createTime, "
            + "u.nickname AS userNickname, u.avatar AS userAvatar "
            + "FROM `comment` c LEFT JOIN `user` u ON u.id = c.user_id "
            + "WHERE c.id = #{id} AND c.deleted = 0")
    CommentVO selectVoById(@Param("id") Long id);

    /**
     * 管理员评论分页列表（平铺展示全部评论，含回复）
     * <p>
     * keyword 同时模糊匹配评论内容、评论人昵称、所属文章标题；
     * articleId 非空时只看该文章下的评论。
     *
     * @param page      分页对象（由 MyBatis-Plus 分页插件自动改写 SQL）
     * @param keyword   搜索关键词（可为 null）
     * @param articleId 文章筛选（可为 null）
     */
    @Select("<script>"
            + "SELECT c.id AS id, c.article_id AS articleId, a.title AS articleTitle, "
            + "c.user_id AS userId, c.parent_id AS parentId, c.content AS content, "
            + "c.create_time AS createTime, u.nickname AS userNickname, u.avatar AS userAvatar "
            + "FROM `comment` c "
            + "LEFT JOIN `user` u ON u.id = c.user_id "
            + "LEFT JOIN article a ON a.id = c.article_id "
            + "WHERE c.deleted = 0 "
            + "<if test='articleId != null'>"
            + "AND c.article_id = #{articleId} "
            + "</if>"
            + "<if test='keyword != null and keyword != \"\"'>"
            + "AND (c.content LIKE CONCAT('%', #{keyword}, '%') "
            + "OR u.nickname LIKE CONCAT('%', #{keyword}, '%') "
            + "OR a.title LIKE CONCAT('%', #{keyword}, '%')) "
            + "</if>"
            + "ORDER BY c.create_time DESC, c.id DESC"
            + "</script>")
    IPage<AdminCommentVO> selectAdminPage(IPage<AdminCommentVO> page,
                                          @Param("keyword") String keyword,
                                          @Param("articleId") Long articleId);

    /**
     * 统计指定用户发表的评论数量（供个人中心使用）
     */
    @Select("SELECT COUNT(*) FROM `comment` WHERE deleted = 0 AND user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);
}
