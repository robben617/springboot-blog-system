package com.example.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 文章-标签关联实体类
 * 对应数据库 article_tag 表（多对多中间表）
 * <p>
 * 注意：该表使用复合主键（article_id, tag_id），没有自增 id 字段，
 * 因此不继承 MyBatis-Plus 的 BaseMapper，统一走 ArticleTagMapper 中的注解 SQL。
 */
@Data
@TableName("article_tag")
public class ArticleTag implements Serializable {

    /** 文章ID（复合主键之一） */
    private Long articleId;

    /** 标签ID（复合主键之一） */
    private Long tagId;
}
