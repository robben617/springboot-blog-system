package com.example.blog.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.blog.common.Constants;

import java.util.List;

/**
 * 分页工具类
 * 将 MyBatis-Plus 的 IPage 转换为通用 PageResult
 */
public class PageUtils {

    /** 每页大小上限，防止前端传入异常值导致一次性拉取全表 */
    private static final long MAX_PAGE_SIZE = 100;

    private PageUtils() {
    }

    /**
     * 规范化页码：为空或小于 1 时统一取第 1 页
     *
     * @param pageNum 原始页码
     * @return 合法页码
     */
    public static long normalizePageNum(Long pageNum) {
        if (pageNum == null || pageNum < 1) {
            return Constants.DEFAULT_PAGE_NUM;
        }
        return pageNum;
    }

    /**
     * 规范化每页大小：为空或小于 1 时取默认值，超过上限则截断到上限
     *
     * @param pageSize 原始每页大小
     * @return 合法每页大小
     */
    public static long normalizePageSize(Long pageSize) {
        if (pageSize == null || pageSize < 1) {
            return Constants.DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
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
