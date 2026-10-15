package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.entity.Tag;
import com.example.blog.vo.ArticleBriefVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 标签 Mapper
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 按标签分页查询已发布文章（C4 按标签浏览）
     * <p>
     * article_tag 是文章与标签的多对多中间表，这里通过 JOIN 取该标签下的文章。
     * 同样只返回 status = 1（已发布）的文章。
     *
     * @param page  分页对象
     * @param tagId 标签ID
     * @return 文章简要信息分页数据
     */
    @Select("SELECT a.id AS id, a.title AS title, a.summary AS summary, a.cover AS cover, "
            + "a.view_count AS viewCount, a.create_time AS createTime, "
            + "a.user_id AS authorId, u.nickname AS authorNickname "
            + "FROM article_tag atg "
            + "JOIN article a ON a.id = atg.article_id "
            + "LEFT JOIN `user` u ON u.id = a.user_id "
            + "WHERE atg.tag_id = #{tagId} AND a.deleted = 0 AND a.status = 1 "
            + "ORDER BY a.create_time DESC, a.id DESC")
    IPage<ArticleBriefVO> selectArticlePage(IPage<ArticleBriefVO> page, @Param("tagId") Long tagId);
}
