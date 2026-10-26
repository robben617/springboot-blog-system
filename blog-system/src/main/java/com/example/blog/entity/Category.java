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
 * 分类实体类
 * 对应数据库 category 表
 */
@Data
@TableName("category")
public class Category implements Serializable {

    /** 分类ID（主键自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称（必填） */
    private String name;

    /** 排序（值越小越靠前） */
    private Integer sort;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 该分类下已发布文章数量（非数据库字段，仅前台分类入口展示用） */
    @TableField(exist = false)
    private Integer articleCount;
}
