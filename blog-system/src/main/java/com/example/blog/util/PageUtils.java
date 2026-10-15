package com.example.blog.util;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

/**
 * 分页工具类
 * 将 MyBatis-Plus 的 IPage 转换为通用 PageResult
 */
public class PageUtils {

    private PageUtils() {
    }

    /**
     * 将 MyBatis-Plus 分页对象转换为 PageResult
     *
     * @param page MyBatis-Plus 分页对象
     * @param <T>  实体类型
     * @return 通用分页结果
     */
    public static <T> PageResult<T> convert(IPage<T> page) {
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    /**
     * 将 MyBatis-Plus 分页对象转换为 PageResult（支持实体转换后的列表）
     *
     * @param page  MyBatis-Plus 分页对象
     * @param list  转换后的数据列表
     * @param <T>   返回数据类型
     * @return 通用分页结果
     */
    public static <T> PageResult<T> convert(IPage<?> page, List<T> list) {
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), list);
    }
}
