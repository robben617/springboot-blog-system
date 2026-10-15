package com.example.blog.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章列表简要信息 VO
 * <p>
 * 用于「按分类浏览」「按标签浏览」等列表场景，只暴露列表页需要的字段，
 * 不返回文章正文、状态等无关信息；作者信息通过 user 表关联查询得到。
 * <p>
 * 说明：本 VO 由分类/标签模块的 Mapper 直接查询 article 表得到，
 * 未新建 Article 实体，避免与文章模块（徐曼玉负责）产生代码冲突。
 */
@Data
public class ArticleBriefVO implements Serializable {

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

    /** 发布时间 */
    private LocalDateTime createTime;

    /** 作者ID */
    private Long authorId;

    /** 作者昵称 */
    private String authorNickname;
}
