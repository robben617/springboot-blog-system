package com.example.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.blog.entity.User;

/**
 * 用户数据访问层
 * 继承 BaseMapper 即拥有 CRUD、分页、条件查询等全部能力
 */
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper 已提供通用方法，如需自定义 SQL 可在此添加
}
