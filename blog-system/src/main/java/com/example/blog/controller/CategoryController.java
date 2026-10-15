package com.example.blog.controller;

import com.example.blog.dto.CategoryDTO;
import com.example.blog.entity.Category;
import com.example.blog.service.CategoryService;
import com.example.blog.util.PageResult;
import com.example.blog.util.Result;
import com.example.blog.vo.ArticleBriefVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类控制器
 * <p>
 * 路径约定：复数 /categories 为公开只读接口，/admin/category 为管理员接口（由 AdminInterceptor 校验权限）。
 * 控制器只负责接收参数与封装返回，业务规则统一放在 CategoryService。
 */
@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 分类列表（公开）
     * GET /categories
     */
    @GetMapping("/categories")
    public Result<List<Category>> listCategories() {
        return Result.success(categoryService.listAll());
    }

    /**
     * 按分类浏览文章（公开，C3）
     * GET /categories/{id}/articles?pageNum=1&pageSize=10
     */
    @GetMapping("/categories/{id}/articles")
    public Result<PageResult<ArticleBriefVO>> listArticlesByCategory(
            @PathVariable("id") Long id,
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(categoryService.pageArticlesByCategory(id, pageNum, pageSize));
    }

    /**
     * 新增分类（管理员，C1）
     * POST /admin/category
     */
    @PostMapping("/admin/category")
    public Result<Category> addCategory(@RequestBody CategoryDTO dto) {
        return Result.success("新增分类成功", categoryService.addCategory(dto));
    }

    /**
     * 修改分类（管理员，C1）
     * PUT /admin/category/{id}
     */
    @PutMapping("/admin/category/{id}")
    public Result<Category> updateCategory(@PathVariable("id") Long id, @RequestBody CategoryDTO dto) {
        return Result.success("修改分类成功", categoryService.updateCategory(id, dto));
    }

    /**
     * 删除分类（管理员，C1）
     * DELETE /admin/category/{id}
     * 说明：分类下存在文章时会被业务层拒绝并返回提示
     */
    @DeleteMapping("/admin/category/{id}")
    public Result<Void> deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除分类成功");
    }
}
