package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.Comment;
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
}
