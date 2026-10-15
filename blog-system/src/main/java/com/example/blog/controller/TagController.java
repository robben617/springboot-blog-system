package com.example.blog.controller;

import com.example.blog.dto.TagDTO;
import com.example.blog.entity.Tag;
import com.example.blog.service.TagService;
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
 * 标签控制器
 * <p>
 * 路径约定：复数 /tags 为公开只读接口，/admin/tag 为管理员接口。
 * 文章与标签的关联维护接口（replaceArticleTags）由文章模块在发布/编辑文章时调用，不在此暴露 HTTP 接口。
 */
@RestController
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 标签列表（公开）
     * GET /tags
     */
    @GetMapping("/tags")
    public Result<List<Tag>> listTags() {
        return Result.success(tagService.listAll());
    }

    /**
     * 按标签浏览文章（公开，C4）
     * GET /tags/{id}/articles?pageNum=1&pageSize=10
     */
    @GetMapping("/tags/{id}/articles")
    public Result<PageResult<ArticleBriefVO>> listArticlesByTag(
            @PathVariable("id") Long id,
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize) {
        return Result.success(tagService.pageArticlesByTag(id, pageNum, pageSize));
    }

    /**
     * 新增标签（管理员，C2）
     * POST /admin/tag
     */
    @PostMapping("/admin/tag")
    public Result<Tag> addTag(@RequestBody TagDTO dto) {
        return Result.success("新增标签成功", tagService.addTag(dto));
    }

    /**
     * 修改标签（管理员，C2）
     * PUT /admin/tag/{id}
     */
    @PutMapping("/admin/tag/{id}")
    public Result<Tag> updateTag(@PathVariable("id") Long id, @RequestBody TagDTO dto) {
        return Result.success("修改标签成功", tagService.updateTag(id, dto));
    }

    /**
     * 删除标签（管理员，C2）
     * DELETE /admin/tag/{id}
     * 说明：删除时自动解除该标签与所有文章的关联，文章本身不受影响
     */
    @DeleteMapping("/admin/tag/{id}")
    public Result<Void> deleteTag(@PathVariable("id") Long id) {
        tagService.deleteTag(id);
        return Result.success("删除标签成功");
    }
}
