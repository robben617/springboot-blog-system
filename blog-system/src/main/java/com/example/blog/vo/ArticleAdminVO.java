package com.example.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 管理员文章列表 VO
 * <p>
 * 用于后台管理的文章列表，包含作者信息（ID、昵称、用户名）和文章状态，
 * 方便管理员按作者筛选和查看文章状态。不返回正文，避免列表页数据过大。
 */
@Data
public class ArticleAdminVO implements Serializable {

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

    /** 分类名称 */
    private String categoryName;

    /** 作者ID */
    private Long authorId;

    /** 作者用户名 */
    private String authorUsername;

    /** 作者昵称 */
    private String authorNickname;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
