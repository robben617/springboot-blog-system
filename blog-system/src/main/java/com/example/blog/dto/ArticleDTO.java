package com.example.blog.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 新建 / 修改文章请求参数
 * <p>
 * status：0-存草稿 1-直接发布；新增时不传默认存草稿，修改时不传则保持原状态不变。
 */
@Data
public class ArticleDTO implements Serializable {

    /** 标题（必填，不超过200字） */
    private String title;

    /** 内容（必填，Markdown） */
    private String content;

    /** 分类ID（可选，传了则校验分类必须存在） */
    private Long categoryId;

    /** 封面图URL（可选） */
    private String cover;

    /** 文章摘要（可选，不超过500字） */
    private String summary;

    /** 标签ID列表（可选；修改时不传表示不改动标签，传空列表表示清空标签） */
    private List<Long> tagIds;

    /** 状态：0-草稿 1-发布（可选） */
    private Integer status;
}
