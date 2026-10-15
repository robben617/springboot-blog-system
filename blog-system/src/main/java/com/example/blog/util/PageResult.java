package com.example.blog.util;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页结果封装
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {

    /** 当前页码 */
    private long pageNum;

    /** 每页大小 */
    private long pageSize;

    /** 总记录数 */
    private long total;

    /** 总页数 */
    private long pages;

    /** 数据列表 */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(long pageNum, long pageSize, long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize;
        this.list = list;
    }
}
