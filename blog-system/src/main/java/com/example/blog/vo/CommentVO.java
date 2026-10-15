package com.example.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 评论展示 VO（两级结构）
 * <p>
 * 一级评论放在列表根节点，其回复放在 replies 中，前端据此缩进一层展示。
 * 只暴露展示所需字段，不含邮箱、密码等用户敏感信息。
 */
@Data
public class CommentVO implements Serializable {

    /** 评论ID */
    private Long id;

    /** 所属文章ID */
    private Long articleId;

    /** 评论人ID */
    private Long userId;

    /** 评论人昵称 */
    private String userNickname;

    /** 评论人头像 */
    private String userAvatar;

    /** 父评论ID（null=一级评论） */
    private Long parentId;

    /** 评论内容 */
    private String content;

    /** 评论时间 */
    private LocalDateTime createTime;

    /** 子回复列表（仅一级评论有此字段有值） */
    private List<CommentVO> replies = new ArrayList<>();
}
