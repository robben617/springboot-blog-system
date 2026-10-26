package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.entity.Category;
import com.example.blog.vo.ArticleBriefVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类 Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 查询全部分类，并统计每个分类下「已发布文章」的数量（前台侧边栏分类入口展示用）
     * <p>
     * 使用 LEFT JOIN + GROUP BY，保证没有文章的空分类也能返回（articleCount 为 0）。
     */
    @Select("SELECT c.id AS id, c.name AS name, c.sort AS sort, c.create_time AS createTime, "
            + "COUNT(a.id) AS articleCount "
            + "FROM category c "
            + "LEFT JOIN article a ON a.category_id = c.id AND a.deleted = 0 AND a.status = 1 "
            + "GROUP BY c.id, c.name, c.sort, c.create_time "
            + "ORDER BY c.sort ASC, c.id ASC")
    List<Category> selectAllWithCount();

    /**
     * 统计某分类下的文章数量（只统计未逻辑删除的文章）
     * 用于「删除分类前必须确认该分类下无关联文章」的业务规则校验
     *
     * @param categoryId 分类ID
     * @return 文章数量
     */
    @Select("SELECT COUNT(*) FROM article WHERE category_id = #{categoryId} AND deleted = 0")
    long countArticles(@Param("categoryId") Long categoryId);

    /**
     * 按分类分页查询已发布文章（C3 按分类浏览）
     * <p>
     * 跨表只读查询：直接读 article / user 表，不新建 Article 实体，避免与文章模块代码冲突。
     * 依据需求「草稿仅作者本人可见」，前台浏览只返回 status = 1（已发布）的文章。
     *
     * @param page       分页对象（由 MyBatis-Plus 分页插件自动改写 SQL）
     * @param categoryId 分类ID
     * @return 文章简要信息分页数据
     */
    @Select("SELECT a.id AS id, a.title AS title, a.summary AS summary, a.cover AS cover, "
            + "a.view_count AS viewCount, a.category_id AS categoryId, c.name AS categoryName, "
            + "a.create_time AS createTime, "
            + "a.user_id AS authorId, u.nickname AS authorNickname "
            + "FROM article a LEFT JOIN `user` u ON u.id = a.user_id "
            + "LEFT JOIN category c ON c.id = a.category_id "
            + "WHERE a.deleted = 0 AND a.status = 1 AND a.category_id = #{categoryId} "
            + "ORDER BY a.create_time DESC, a.id DESC")
    IPage<ArticleBriefVO> selectArticlePage(IPage<ArticleBriefVO> page, @Param("categoryId") Long categoryId);
}
