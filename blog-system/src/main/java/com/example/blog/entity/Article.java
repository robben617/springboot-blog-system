package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章实体类
 * 对应数据库 article 表
 */
@Data
@TableName("article")
public class Article implements Serializable {

    /** 文章ID（主键自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 作者ID */
    private Long userId;

    /** 分类ID（可为空） */
    private Long categoryId;

    /** 标题 */
    private String title;

    /** 封面图URL */
    private String cover;

    /** 文章摘要 */
    private String summary;

    /** 内容（Markdown） */
    private String content;

    /** 浏览量 */
    private Integer viewCount;

    /** 状态：0-草稿 1-已发布 */
    private Integer status;

    /** 逻辑删除：0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
