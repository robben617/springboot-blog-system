package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.TagDTO;
import com.example.blog.entity.Tag;
import com.example.blog.util.PageResult;
import com.example.blog.vo.ArticleBriefVO;

import java.util.List;

/**
 * 标签业务层接口
 */
public interface TagService extends IService<Tag> {

    /**
     * 查询全部标签（按名称排序）
     */
    List<Tag> listAll();

    /**
     * 新增标签（C2）
     */
    Tag addTag(TagDTO dto);

    /**
     * 修改标签（C2）
     */
    Tag updateTag(Long id, TagDTO dto);

    /**
     * 删除标签（C2）
     * 业务规则：删除标签时自动解除该标签与所有文章的关联，文章本身不受影响
     */
    void deleteTag(Long id);

    /**
     * 按标签分页查询已发布文章（C4）
     */
    PageResult<ArticleBriefVO> pageArticlesByTag(Long tagId, Long pageNum, Long pageSize);

    /**
     * 重置某篇文章的标签（供文章模块发布/编辑文章时调用）
     *
     * @param articleId 文章ID
     * @param tagIds    标签ID列表，传空集合表示清空标签
     */
    void replaceArticleTags(Long articleId, List<Long> tagIds);

    /**
     * 查询某篇文章关联的标签ID列表（供文章模块回显标签时调用）
     */
    List<Long> listTagIdsByArticleId(Long articleId);
}
