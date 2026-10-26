package com.example.blog.service;

import com.example.blog.vo.AdminStatsVO;

/**
 * 后台管理统计业务层接口
 */
public interface AdminStatsService {

    /**
     * 获取后台概览统计数据（文章 / 用户 / 评论 / 浏览量）
     */
    AdminStatsVO getStats();
}
