package com.example.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.blog.common.Constants;
import com.example.blog.entity.User;
import com.example.blog.exception.BusinessException;
import com.example.blog.mapper.UserMapper;
import com.example.blog.service.UserService;
import com.example.blog.util.PasswordUtil;
import org.springframework.stereotype.Service;

/**
 * 用户业务层实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public User register(String username, String password, String email) {
        // 1. 检查用户名是否已存在
        User exist = getByUsername(username);
        if (exist != null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "用户名已存在");
        }

        // 2. 生成盐值 + 加密密码
        String salt = PasswordUtil.generateSalt();
        String encrypted = PasswordUtil.encrypt(password, salt);

        // 3. 构建用户对象并保存
        User user = new User();
        user.setUsername(username);
        user.setPassword(encrypted);
        user.setSalt(salt);
        user.setNickname(username);       // 默认昵称 = 用户名
        user.setEmail(email);
        user.setRole(Constants.ROLE_USER); // 默认普通用户
        user.setStatus(Constants.STATUS_ENABLED); // 默认启用
        save(user);

        return user;
    }

    @Override
    public User login(String username, String password) {
        // 1. 查用户
        User user = getByUsername(username);
        if (user == null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "用户名或密码错误");
        }

        // 2. 验证账号状态
        if (Constants.STATUS_DISABLED == user.getStatus()) {
            throw new BusinessException(Constants.CODE_FORBIDDEN, "账号已被禁用，请联系管理员");
        }

        // 3. 校验密码
        if (!PasswordUtil.matches(password, user.getSalt(), user.getPassword())) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "用户名或密码错误");
        }

        return user;
    }
}
