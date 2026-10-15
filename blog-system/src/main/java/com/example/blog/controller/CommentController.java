package com.example.blog.controller;

import com.example.blog.dto.CommentDTO;
import com.example.blog.service.CommentService;
import com.example.blog.util.Result;
import com.example.blog.vo.CommentVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 评论控制器
 * <p>
 * 路径约定：复数 /comments/article/{id} 为公开只读接口（评论展示）；
 * 单数 /comment 为写操作，需要登录（由 LoginInterceptor 拦截）。
 * 当前登录用户信息由 LoginInterceptor 写入 request 属性（userId / role）。
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 评论展示（公开，M3）
     * GET /comments/article/{articleId}
     * 返回两级结构：一级评论 + 其下的 replies
     */
    @GetMapping("/comments/article/{articleId}")
    public Result<List<CommentVO>> listComments(@PathVariable("articleId") Long articleId) {
        return Result.success(commentService.listByArticle(articleId));
    }

    /**
     * 发表评论 / 回复评论（需登录，M1 / M2）
     * POST /comment
     * body：{ "articleId": 1, "content": "写得不错", "parentId": null }
     * parentId 为空表示发表一级评论；非空表示回复该评论
     */
    @PostMapping("/comment")
    public Result<CommentVO> addComment(@RequestBody CommentDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("评论成功", commentService.addComment(userId, dto));
    }

    /**
     * 删除评论（需登录，M4）
     * DELETE /comment/{id}
     * 权限规则：管理员可删除全站任意评论；文章作者可删除自己文章下的任意评论；删除时一并删除其回复
     */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable("id") Long id, HttpServletRequest request) {
        Long operatorId = (Long) request.getAttribute("userId");
        Integer operatorRole = (Integer) request.getAttribute("role");
        commentService.deleteComment(id, operatorId, operatorRole);
        return Result.success("删除评论成功");
    }
}
