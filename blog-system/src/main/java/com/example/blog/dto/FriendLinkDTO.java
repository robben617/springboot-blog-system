package com.example.blog.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 友情链接新增/修改请求参数
 */
@Data
public class FriendLinkDTO implements Serializable {

    /** 网站名称 */
    private String name;

    /** 网站地址 */
    private String url;

    /** 排序（值越小越靠前，不传默认为 0） */
    private Integer sort;
}
