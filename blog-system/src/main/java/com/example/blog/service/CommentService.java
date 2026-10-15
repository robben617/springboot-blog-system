package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.dto.CommentDTO;
import com.example.blog.entity.Comment;
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
}
