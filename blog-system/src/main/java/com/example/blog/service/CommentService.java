package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.CommentDTO;
import com.example.blog.entity.Comment;
import com.example.blog.util.PageResult;
import com.example.blog.vo.AdminCommentVO;
import com.example.blog.vo.CommentVO;

import java.util.List;

/**
 * 评论业务层接口
 */
public interface CommentService extends IService<Comment> {

    /**
     * 查询某篇文章的评论列表（两级结构，M3）
     *
     * @return 一级评论列表，每条一级评论的 replies 中挂其回复
     */
    List<CommentVO> listByArticle(Long articleId);

    /**
     * 发表评论 / 回复评论（M1、M2）
     *
     * @param userId 当前登录用户ID
     * @param dto    评论参数（parentId 为空表示一级评论）
     */
    CommentVO addComment(Long userId, CommentDTO dto);

    /**
     * 删除评论（M4）
     * 权限规则：管理员可删除全站任意评论；文章作者可删除自己文章下的任意评论。
     * 删除时一并删除该评论下的全部回复。
     *
     * @param commentId    评论ID
     * @param operatorId   当前登录用户ID
     * @param operatorRole 当前登录用户角色
     */
    void deleteComment(Long commentId, Long operatorId, Integer operatorRole);

    /**
     * 管理员评论分页列表（平铺展示全部评论，含回复）
     *
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @param keyword   搜索关键词（匹配评论内容 / 评论人昵称 / 文章标题），可为 null
     * @param articleId 只看指定文章下的评论，可为 null
     */
    PageResult<AdminCommentVO> pageAdminComments(Long pageNum, Long pageSize, String keyword, Long articleId);
}
