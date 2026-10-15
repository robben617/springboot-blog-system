package com.example.blog.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 标签新增/修改请求参数
 */
@Data
public class TagDTO implements Serializable {

    /** 标签名称 */
    private String name;
}
