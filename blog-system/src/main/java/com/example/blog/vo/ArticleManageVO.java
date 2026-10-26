package com.example.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 我的文章列表 VO
 * <p>
 * 用于作者管理自己的文章（含草稿），相比公开的 ArticleBriefVO 多了 status 字段，
 * 以便作者区分「草稿」与「已发布」；不返回正文，避免列表页数据过大。
 */
@Data
public class ArticleManageVO implements Serializable {

    /** 文章ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 摘要 */
    private String summary;

    /** 封面图URL */
    private String cover;

    /** 浏览量 */
    private Integer viewCount;

    /** 状态：0-草稿 1-已发布 */
    private Integer status;

    /** 分类ID */
    private Long categoryId;

    /** 分类名称（列表展示所属分类，未分类时为 null） */
    private String categoryName;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
