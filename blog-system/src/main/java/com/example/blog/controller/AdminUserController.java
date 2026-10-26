package com.example.blog.controller;

import com.example.blog.dto.UserStatusDTO;
import com.example.blog.service.UserManageService;
import com.example.blog.util.PageResult;
import com.example.blog.util.Result;
import com.example.blog.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器（仅管理员）
 * <p>
 * 路径约定：/admin/** 由 AdminInterceptor 统一校验管理员权限，
 * 无需额外配置，也不会影响任何已有接口。
 */
@RestController
@RequiredArgsConstructor
public class AdminUserController {

    private final UserManageService userManageService;

    /**
     * 用户分页列表 / 搜索（管理员）
     * GET /admin/users?pageNum=1&pageSize=10&keyword=关键词&status=1
     * keyword 按用户名或昵称模糊匹配；status 不传为全部
     */
    @GetMapping("/admin/users")
    public Result<PageResult<UserVO>> pageUsers(
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "status", required = false) Integer status) {
        return Result.success(userManageService.pageUsers(pageNum, pageSize, keyword, status));
    }

    /**
     * 启用 / 禁用用户（管理员）
     * PUT /admin/user/{id}/status
     * body：{ "status": 0 }   0-禁用 1-启用
     */
    @PutMapping("/admin/user/{id}/status")
    public Result<UserVO> updateStatus(@PathVariable("id") Long id, @RequestBody UserStatusDTO dto) {
        Integer status = dto == null ? null : dto.getStatus();
        return Result.success("用户状态修改成功", userManageService.updateStatus(id, status));
    }

    /**
     * 删除用户（管理员，逻辑删除）
     * DELETE /admin/user/{id}
     * 会级联删除该用户发布的全部文章、草稿与评论；管理员账号不可被删除
     */
    @DeleteMapping("/admin/user/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long id) {
        userManageService.deleteUser(id);
        return Result.success("删除用户成功");
    }

    /**
     * 重置用户密码为系统初始密码（管理员）
     * PUT /admin/user/{id}/reset-password
     * 说明：系统不提供自助密码找回功能，忘记密码时由管理员在后台重置为初始密码
     */
    @PutMapping("/admin/user/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable("id") Long id) {
        userManageService.resetPassword(id);
        return Result.success("密码已重置为初始密码");
    }
}
