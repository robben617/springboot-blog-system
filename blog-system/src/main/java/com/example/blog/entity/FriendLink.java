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
 * 友情链接实体类
 * 对应数据库 friend_link 表
 */
@Data
@TableName("friend_link")
public class FriendLink implements Serializable {

    /** 友链ID（主键自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 网站名称（必填） */
    private String name;

    /** 网站地址（必填，http/https 开头） */
    private String url;

    /** 排序（值越小越靠前） */
    private Integer sort;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
