package com.example.blog.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 发表评论 / 回复评论请求参数
 */
@Data
public class CommentDTO implements Serializable {

    /** 所属文章ID */
    private Long articleId;

    /** 评论内容 */
    private String content;

    /**
     * 父评论ID
     * null = 发表一级评论；非空 = 回复该评论（只允许回复一级评论，回复的回复会被自动归并到一级评论下）
     */
    private Long parentId;
}
