package com.example.blog.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章-标签关联 Mapper
 * <p>
 * article_tag 使用复合主键（article_id, tag_id）且没有独立 id 字段，
 * 不适用 MyBatis-Plus BaseMapper 的单主键 CRUD，因此这里全部使用注解 SQL。
 * <p>
 * 中间表是纯关联关系表，没有逻辑删除字段，删除即物理删除。
 */
@Mapper
public interface ArticleTagMapper {

    /**
     * 建立文章与标签的关联
     */
    @Insert("INSERT INTO article_tag (article_id, tag_id) VALUES (#{articleId}, #{tagId})")
    int insert(@Param("articleId") Long articleId, @Param("tagId") Long tagId);

    /**
     * 解除某标签与所有文章的关联（删除标签时调用）
     *
     * @return 被解除的关联条数
     */
    @Delete("DELETE FROM article_tag WHERE tag_id = #{tagId}")
    int deleteByTagId(@Param("tagId") Long tagId);

    /**
     * 解除某文章与所有标签的关联（文章删除或重新设置标签时调用，供文章模块复用）
     */
    @Delete("DELETE FROM article_tag WHERE article_id = #{articleId}")
    int deleteByArticleId(@Param("articleId") Long articleId);

    /**
     * 查询某篇文章关联的全部标签ID（供文章详情展示标签使用）
     */
    @Select("SELECT tag_id FROM article_tag WHERE article_id = #{articleId}")
    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);
}
