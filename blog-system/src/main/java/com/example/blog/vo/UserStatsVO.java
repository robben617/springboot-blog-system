package com.example.blog.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 个人中心统计 VO
 * <p>
 * 用于个人中心展示当前用户的创作数据：文章数量、评论数量与累计浏览量。
 */
@Data
public class UserStatsVO implements Serializable {

    /** 文章总数（含草稿） */
    private long articleCount;

    /** 已发布文章数 */
    private long publishedCount;

    /** 草稿数 */
    private long draftCount;

    /** 评论总数 */
    private long commentCount;

    /** 文章累计浏览量 */
    private long viewCount;
}
