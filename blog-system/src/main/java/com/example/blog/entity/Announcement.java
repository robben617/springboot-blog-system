package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公告实体类
 * 对应数据库 announcement 表
 */
@Data
@TableName("announcement")
public class Announcement implements Serializable {

    /** 公告ID（主键自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标题（必填） */
    private String title;

    /** 内容（必填） */
    private String content;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
