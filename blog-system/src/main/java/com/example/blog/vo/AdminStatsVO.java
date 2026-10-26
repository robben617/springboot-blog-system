package com.example.blog.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 后台管理概览统计 VO
 * <p>
 * 供管理后台首页的统计卡片展示，避免前端写死假数据。
 */
@Data
public class AdminStatsVO implements Serializable {

    /** 文章总数（含草稿，不含已删除） */
    private Long articleCount;

    /** 已发布文章数 */
    private Long publishedCount;

    /** 草稿数 */
    private Long draftCount;

    /** 注册用户数（不含已删除） */
    private Long userCount;

    /** 评论总数（不含已删除） */
    private Long commentCount;

    /** 全站总浏览量 */
    private Long totalViews;
}
