package com.example.blog.controller;

import com.example.blog.service.ArticleService;
import com.example.blog.util.PageResult;
import com.example.blog.util.Result;
import com.example.blog.vo.ArticleAdminVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员文章管理控制器
 * <p>
 * 路径以 /admin 开头，由 AdminInterceptor 统一校验管理员权限（role=1）。
 * 提供管理员对全站文章的列表查询能力，支持多维度筛选。
 */
@RestController
@RequiredArgsConstructor
public class AdminArticleController {

    private final ArticleService articleService;

    /**
     * 管理员文章分页列表 / 搜索
     * GET /admin/articles?pageNum=1&pageSize=10&keyword=关键词&status=1&categoryId=1
     * <p>
     * 管理员可查看全站所有文章（含草稿），支持以下筛选条件：
     * - keyword：模糊匹配标题、作者用户名、作者昵称
     * - status：0-只看草稿 1-只看已发布，不传为全部
     * - categoryId：按分类筛选，不传为全部
     *
     * @param pageNum    页码，默认 1
     * @param pageSize   每页大小，默认 10
     * @param keyword    搜索关键词
     * @param status     状态筛选
     * @param categoryId 分类筛选
     * @return 分页文章列表（管理员视图）
     */
    @GetMapping("/admin/articles")
    public Result<PageResult<ArticleAdminVO>> pageAdminArticles(
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        return Result.success(articleService.pageAdminArticles(pageNum, pageSize, keyword, status, categoryId));
    }
}
