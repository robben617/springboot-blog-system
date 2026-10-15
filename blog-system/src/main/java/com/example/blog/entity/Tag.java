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
 * 标签实体类
 * 对应数据库 tag 表
 */
@Data
@TableName("tag")
public class Tag implements Serializable {

    /** 标签ID（主键自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名称（必填，唯一） */
    private String name;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
