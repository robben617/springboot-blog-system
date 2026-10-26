package com.example.blog.controller;

import com.example.blog.common.Constants;
import com.example.blog.dto.ArticleDTO;
import com.example.blog.entity.Article;
import com.example.blog.service.ArticleService;
import com.example.blog.util.PageResult;
import com.example.blog.util.Result;
import com.example.blog.vo.ArticleBriefVO;
import com.example.blog.vo.ArticleDetailVO;
import com.example.blog.vo.ArticleManageVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文章控制器
 * <p>
 * 路径约定（与分类/评论模块一致）：
 * 复数 /articles、/articles/{id} 为公开只读接口（游客可访问，已在 WebMvcConfig 放行）；
 * 单数 /article/** 为写操作与「我的文章」，需要登录（由 LoginInterceptor 拦截并写入 userId / role）。
 * <p>
 * 注意：公开接口不走 LoginInterceptor，因此详情接口直接从 Session 读取当前用户，
 * 用于「草稿仅作者本人或管理员可见」的判定。
 */
@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 文章分页列表 / 搜索（公开）
     * GET /articles?pageNum=1&pageSize=10&keyword=关键词
     * 只返回已发布文章；keyword 为空时为全量列表，非空时按标题/摘要模糊搜索
     */
    @GetMapping("/articles")
    public Result<PageResult<ArticleBriefVO>> pagePublished(
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(articleService.pagePublished(pageNum, pageSize, keyword));
    }

    /**
     * 文章详情（公开，浏览量 +1）
     * GET /articles/{id}
     * 草稿仅作者本人或管理员可见，其余人返回 404
     */
    @GetMapping("/articles/{id}")
    public Result<ArticleDetailVO> getDetail(@PathVariable("id") Long id, HttpServletRequest request) {
        // 公开路径未经过 LoginInterceptor，改从 Session 直接读取登录信息（游客为 null）
        HttpSession session = request.getSession(false);
        Long currentUserId = session == null ? null : (Long) session.getAttribute(Constants.SESSION_USER_ID);
        Integer currentRole = session == null ? null : (Integer) session.getAttribute(Constants.SESSION_ROLE);
        return Result.success(articleService.getDetail(id, currentUserId, currentRole));
    }

    /**
     * 编辑回显（需登录，仅作者本人或管理员）
     * GET /article/{id}/edit
     * 与公开详情接口不同，不增加浏览量，供文章编辑器加载原内容
     */
    @GetMapping("/article/{id}/edit")
    public Result<ArticleDetailVO> getDetailForEdit(@PathVariable("id") Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Integer role = (Integer) request.getAttribute("role");
        return Result.success(articleService.getDetailForEdit(id, userId, role));
    }

    /**
     * 我的文章分页列表（需登录，含草稿）
     * GET /article/my?pageNum=1&pageSize=10&status=0
     * status 可选：0-只看草稿 1-只看已发布，不传为全部
     */
    @GetMapping("/article/my")
    public Result<PageResult<ArticleManageVO>> pageMyArticles(
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
            @RequestParam(value = "status", required = false) Integer status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(articleService.pageMyArticles(userId, status, pageNum, pageSize));
    }

    /**
     * 新建文章（需登录）
     * POST /article
     * body：{ "title": "...", "content": "...", "categoryId": 1, "cover": "...",
     *         "summary": "...", "tagIds": [1, 2], "status": 1 }
     * status 不传默认存草稿；传 1 表示直接发布
     */
    @PostMapping("/article")
    public Result<Article> addArticle(@RequestBody ArticleDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("文章保存成功", articleService.addArticle(userId, dto));
    }

    /**
     * 修改文章（需登录，仅作者本人或管理员）
     * PUT /article/{id}
     * status 不传则保持原状态；tagIds 不传则不改动标签
     */
    @PutMapping("/article/{id}")
    public Result<Article> updateArticle(@PathVariable("id") Long id, @RequestBody ArticleDTO dto,
                                         HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        Integer operatorRole = (Integer) request.getAttribute("role");
        return Result.success("文章修改成功", articleService.updateArticle(id, operatorId, operatorRole, dto));
    }

    /**
     * 删除文章（需登录，仅作者本人或管理员；逻辑删除）
     * DELETE /article/{id}
     */
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable("id") Long id, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        Integer operatorRole = (Integer) request.getAttribute("role");
        articleService.deleteArticle(id, operatorId, operatorRole);
        return Result.success("删除文章成功");
    }

    /**
     * 发布文章（需登录，草稿 -> 已发布，仅作者本人或管理员）
     * PUT /article/{id}/publish
     */
    @PutMapping("/article/{id}/publish")
    public Result<Void> publishArticle(@PathVariable("id") Long id, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        Integer operatorRole = (Integer) request.getAttribute("role");
        articleService.publishArticle(id, operatorId, operatorRole);
        return Result.success("文章发布成功");
    }

    /**
     * 转为草稿（需登录，已发布 -> 草稿，仅作者本人或管理员）
     * PUT /article/{id}/draft
     */
    @PutMapping("/article/{id}/draft")
    public Result<Void> unpublishArticle(@PathVariable("id") Long id, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        Integer operatorRole = (Integer) request.getAttribute("role");
        articleService.unpublishArticle(id, operatorId, operatorRole);
        return Result.success("文章已转为草稿");
    }
}
