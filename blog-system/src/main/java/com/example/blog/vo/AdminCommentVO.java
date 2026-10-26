package com.example.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员评论列表 VO
 * <p>
 * 用于后台评论管理：相比前台的两级结构，这里平铺展示全部评论（含回复），
 * 并带上所属文章标题与评论人昵称，便于管理员跨文章检索与定位。
 */
@Data
public class AdminCommentVO implements Serializable {

    /** 评论ID */
    private Long id;

    /** 所属文章ID */
    private Long articleId;

    /** 所属文章标题 */
    private String articleTitle;

    /** 评论人ID */
    private Long userId;

    /** 评论人昵称 */
    private String userNickname;

    /** 评论人头像 */
    private String userAvatar;

    /** 父评论ID（null=一级评论，非空=回复） */
    private Long parentId;

    /** 评论内容 */
    private String content;

    /** 评论时间 */
    private LocalDateTime createTime;
}
