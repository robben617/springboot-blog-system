package com.example.blog.vo;

import com.example.blog.entity.Tag;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章详情 VO
 * <p>
 * 用于文章详情页，包含正文、分类名称、作者信息与关联标签列表。
 * 作者 / 分类信息通过 user、category 表关联查询得到，标签通过 article_tag 关联查询得到。
 */
@Data
public class ArticleDetailVO implements Serializable {

    /** 文章ID */
    private Long id;

    /** 标题 */
    private String title;

    /** 内容（Markdown） */
    private String content;

    /** 封面图URL */
    private String cover;

    /** 摘要 */
    private String summary;

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

    /** 作者昵称 */
    private String authorNickname;

    /** 作者头像URL */
    private String authorAvatar;

    /** 关联标签列表 */
    private List<Tag> tags;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
