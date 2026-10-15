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
 * 评论实体类
 * 对应数据库 comment 表
 * <p>
 * 两级评论结构：parentId 为 null 表示一级评论（直接评论文章），
 * 非空表示对某条一级评论的回复（回复不支持再嵌套）。
 */
@Data
@TableName("comment")
public class Comment implements Serializable {

    /** 评论ID（主键自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属文章ID */
    private Long articleId;

    /** 评论人ID */
    private Long userId;

    /** 父评论ID（null=一级评论；非空=回复，指向同表一级评论的id） */
    private Long parentId;

    /** 评论内容（不超过500字） */
    private String content;

    /** 逻辑删除：0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
