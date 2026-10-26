package com.example.blog.controller;

import com.example.blog.service.CommentService;
import com.example.blog.util.PageResult;
import com.example.blog.util.Result;
import com.example.blog.vo.AdminCommentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员评论管理控制器
 * <p>
 * 路径以 /admin 开头，由 AdminInterceptor 统一校验管理员权限（role=1）。
 * 删除评论复用 /comment/{id}（业务层已放行管理员），此处只提供列表查询。
 */
@RestController
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    /**
     * 管理员评论分页列表 / 搜索
     * GET /admin/comments?pageNum=1&pageSize=10&keyword=关键词&articleId=1
     * <p>
     * 平铺返回全部评论（含回复），keyword 同时匹配评论内容、评论人昵称与文章标题。
     *
     * @param pageNum   页码，默认 1
     * @param pageSize  每页大小，默认 10
     * @param keyword   搜索关键词
     * @param articleId 只看指定文章下的评论，不传为全部
     */
    @GetMapping("/admin/comments")
    public Result<PageResult<AdminCommentVO>> pageAdminComments(
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "articleId", required = false) Long articleId) {
        return Result.success(commentService.pageAdminComments(pageNum, pageSize, keyword, articleId));
    }
}
