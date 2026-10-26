package com.example.blog.controller;

import com.example.blog.dto.ChangePasswordDTO;
import com.example.blog.dto.UpdateProfileDTO;
import com.example.blog.service.UserManageService;
import com.example.blog.util.Result;
import com.example.blog.vo.UserVO;
import com.example.blog.vo.UserStatsVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 个人资料控制器（需登录）
 * <p>
 * 路径约定：/user/** 属于登录后接口，由 LoginInterceptor 统一拦截，
 * 当前登录用户信息由拦截器写入 request 属性（userId）。
 * <p>
 * 说明：本控制器只新增「修改资料 / 修改密码 / 上传头像」三个接口；
 * 查看当前用户信息（GET /user/info）等已有接口保留在原 UserController 中，不做改动。
 */
@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UserManageService userManageService;

    /**
     * 个人创作统计（需登录）
     * GET /user/stats
     */
    @GetMapping("/user/stats")
    public Result<UserStatsVO> getUserStats(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(userManageService.getUserStats(userId));
    }

    /**
     * 修改个人资料（需登录）
     * PUT /user/profile
     * body：{ "nickname": "...", "email": "...", "avatar": "...", "bio": "..." }
     * 只更新传入的字段；未传的字段保持原值
     */
    @PutMapping("/user/profile")
    public Result<UserVO> updateProfile(@RequestBody UpdateProfileDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success("个人资料修改成功", userManageService.updateProfile(userId, dto));
    }

    /**
     * 修改密码（需登录）
     * PUT /user/password
     * body：{ "oldPassword": "...", "newPassword": "..." }
     */
    @PutMapping("/user/password")
    public Result<Void> changePassword(@RequestBody ChangePasswordDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userManageService.changePassword(userId, dto);
        return Result.success("密码修改成功");
    }

    /**
     * 上传头像（需登录）
     * POST /user/avatar
     * form-data：file=头像图片（不超过2MB）
     * 返回可直接访问的头像相对路径，前端拿到后可通过 PUT /user/profile 写入 avatar 字段
     */
    @PostMapping("/user/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return Result.success("头像上传成功", userManageService.uploadAvatar(file));
    }
}
