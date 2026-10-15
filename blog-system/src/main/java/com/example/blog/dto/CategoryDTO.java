package com.example.blog.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类新增/修改请求参数
 */
@Data
public class CategoryDTO implements Serializable {

    /** 分类名称 */
    private String name;

    /** 排序（值越小越靠前，不传默认为 0） */
    private Integer sort;
}
