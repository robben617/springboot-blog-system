package com.example.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.blog.entity.User;

/**
 * 用户业务层接口
 */
public interface UserService extends IService<User> {

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 注册新用户（加密密码 + 生成盐值）
     *
     * @param username 用户名
     * @param password 明文密码
     * @param email    邮箱
     * @return 注册成功的用户
     */
    User register(String username, String password, String email);

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 明文密码
     * @return 用户对象，验证失败返回null
     */
    User login(String username, String password);
}
