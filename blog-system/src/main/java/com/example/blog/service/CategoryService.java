package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.CategoryDTO;
import com.example.blog.entity.Category;
import com.example.blog.util.PageResult;
import com.example.blog.vo.ArticleBriefVO;

import java.util.List;

/**
 * 分类业务层接口
 */
public interface CategoryService extends IService<Category> {

    /**
     * 查询全部分类（按 sort 升序、id 升序）
     */
    List<Category> listAll();

    /**
     * 新增分类（C1）
     */
    Category addCategory(CategoryDTO dto);

    /**
     * 修改分类（C1）
     */
    Category updateCategory(Long id, CategoryDTO dto);

    /**
     * 删除分类（C1）
     * 业务规则：分类下存在文章时禁止删除
     */
    void deleteCategory(Long id);

    /**
     * 按分类分页查询已发布文章（C3）
     */
    PageResult<ArticleBriefVO> pageArticlesByCategory(Long categoryId, Long pageNum, Long pageSize);
}
