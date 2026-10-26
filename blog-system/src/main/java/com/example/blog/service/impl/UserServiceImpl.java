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

    /** 用户名最小长度 */
    private static final int USERNAME_MIN_LENGTH = 3;
    /** 用户名最大长度 */
    private static final int USERNAME_MAX_LENGTH = 20;
    /** 密码最小长度 */
    private static final int PASSWORD_MIN_LENGTH = 6;
    /** 密码最大长度 */
    private static final int PASSWORD_MAX_LENGTH = 20;

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));
    }

    @Override
    public User register(String username, String password, String email) {
        // 1. 校验用户名格式：3~20 位，仅允许字母、数字、下划线
        validateUsernameFormat(username);

        // 2. 校验密码强度：6~20 位，须同时包含字母和数字
        validatePasswordFormat(password);

        // 3. 检查用户名是否已存在
        User exist = getByUsername(username);
        if (exist != null) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "用户名已存在");
        }

        // 4. 生成盐值 + 加密密码
        String salt = PasswordUtil.generateSalt();
        String encrypted = PasswordUtil.encrypt(password, salt);

        // 5. 构建用户对象并保存
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

    /**
     * 校验用户名格式
     * 规则：长度 3~20 位，仅允许字母、数字、下划线
     */
    private void validateUsernameFormat(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "用户名不能为空");
        }
        String trimmed = username.trim();
        if (trimmed.length() < USERNAME_MIN_LENGTH || trimmed.length() > USERNAME_MAX_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST,
                    "用户名长度须为 " + USERNAME_MIN_LENGTH + "~" + USERNAME_MAX_LENGTH + " 位");
        }
        if (!trimmed.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "用户名仅允许字母、数字和下划线");
        }
    }

    /**
     * 校验密码强度
     * 规则：长度 6~20 位，须同时包含字母和数字，可包含特殊字符
     */
    private void validatePasswordFormat(String password) {
        if (password == null || password.isEmpty()) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "密码不能为空");
        }
        if (password.length() < PASSWORD_MIN_LENGTH || password.length() > PASSWORD_MAX_LENGTH) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST,
                    "密码长度须为 " + PASSWORD_MIN_LENGTH + "~" + PASSWORD_MAX_LENGTH + " 位");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasLetter && hasDigit) {
                break;
            }
        }
        if (!hasLetter || !hasDigit) {
            throw new BusinessException(Constants.CODE_BAD_REQUEST, "密码须同时包含字母和数字");
        }
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
