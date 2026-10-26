package com.example.blog.controller;

import com.example.blog.dto.FriendLinkDTO;
import com.example.blog.entity.FriendLink;
import com.example.blog.service.FriendLinkService;
import com.example.blog.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 友情链接控制器
 * <p>
 * 路径约定：复数 /friend-links 为公开只读接口（游客可访问），
 * /admin/friend-link 为管理员接口（由 AdminInterceptor 校验权限）。
 */
@RestController
@RequiredArgsConstructor
public class FriendLinkController {

    private final FriendLinkService friendLinkService;

    /**
     * 友链列表（公开）
     * GET /friend-links
     */
    @GetMapping("/friend-links")
    public Result<List<FriendLink>> listFriendLinks() {
        return Result.success(friendLinkService.listAll());
    }

    /**
     * 新增友链（管理员）
     * POST /admin/friend-link
     */
    @PostMapping("/admin/friend-link")
    public Result<FriendLink> addFriendLink(@RequestBody FriendLinkDTO dto) {
        return Result.success("新增友链成功", friendLinkService.addFriendLink(dto));
    }

    /**
     * 修改友链（管理员）
     * PUT /admin/friend-link/{id}
     */
    @PutMapping("/admin/friend-link/{id}")
    public Result<FriendLink> updateFriendLink(@PathVariable("id") Long id, @RequestBody FriendLinkDTO dto) {
        return Result.success("修改友链成功", friendLinkService.updateFriendLink(id, dto));
    }

    /**
     * 删除友链（管理员）
     * DELETE /admin/friend-link/{id}
     */
    @DeleteMapping("/admin/friend-link/{id}")
    public Result<Void> deleteFriendLink(@PathVariable("id") Long id) {
        friendLinkService.deleteFriendLink(id);
        return Result.success("删除友链成功");
    }
}
