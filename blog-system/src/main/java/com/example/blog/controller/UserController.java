package com.example.blog.controller;

import com.example.blog.common.Constants;
import com.example.blog.dto.LoginDTO;
import com.example.blog.dto.RegisterDTO;
import com.example.blog.entity.User;
import com.example.blog.service.UserService;
import com.example.blog.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 * 处理注册、登录、个人信息等用户相关请求
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户注册
     * POST /register
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterDTO dto) {
        // 简单参数校验
        if (dto.getUsername() == null || dto.getUsername().isBlank()) {
            return Result.error(Constants.CODE_BAD_REQUEST, "用户名不能为空");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            return Result.error(Constants.CODE_BAD_REQUEST, "密码不能为空");
        }
        if (dto.getPassword().length() < 6) {
            return Result.error(Constants.CODE_BAD_REQUEST, "密码至少6位");
        }

        User user = userService.register(dto.getUsername(), dto.getPassword(), dto.getEmail());
        // 密码和盐值不返回给前端
        Map<String, Object> data = buildUserInfo(user);
        return Result.success("注册成功", data);
    }

    /**
     * 用户登录
     * POST /login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto, HttpSession session) {
        if (dto.getUsername() == null || dto.getPassword() == null) {
            return Result.error(Constants.CODE_BAD_REQUEST, "用户名和密码不能为空");
        }

        User user = userService.login(dto.getUsername(), dto.getPassword());

        // 登录成功，把用户信息存入 Session
        session.setAttribute(Constants.SESSION_USER_ID, user.getId());
        session.setAttribute(Constants.SESSION_USERNAME, user.getUsername());
        session.setAttribute(Constants.SESSION_ROLE, user.getRole());

        Map<String, Object> data = buildUserInfo(user);
        return Result.success("登录成功", data);
    }

    /**
     * 退出登录
     * POST /logout
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success("已退出登录");
    }

    /**
     * 获取当前登录用户信息
     * GET /user/info
     */
    @GetMapping("/user/info")
    public Result<Map<String, Object>> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(Constants.CODE_NOT_FOUND, "用户不存在");
        }
        return Result.success(buildUserInfo(user));
    }

    /**
     * 根据ID查询用户（需登录）
     * GET /user/{id}
     */
    @GetMapping("/user/{id}")
    public Result<Map<String, Object>> getUserById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error(Constants.CODE_NOT_FOUND, "用户不存在");
        }
        return Result.success(buildUserInfo(user));
    }

    /**
     * 构建返回给前端的用户信息（去掉密码和盐值）
     */
    private Map<String, Object> buildUserInfo(User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("email", user.getEmail());
        data.put("avatar", user.getAvatar());
        data.put("bio", user.getBio());
        data.put("role", user.getRole());
        data.put("createTime", user.getCreateTime());
        return data;
    }
}
