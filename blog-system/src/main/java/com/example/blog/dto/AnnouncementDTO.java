package com.example.blog.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 公告新增/修改请求参数
 */
@Data
public class AnnouncementDTO implements Serializable {

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;
}
