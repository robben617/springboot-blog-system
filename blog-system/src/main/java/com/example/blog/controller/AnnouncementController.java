package com.example.blog.controller;

import com.example.blog.dto.AnnouncementDTO;
import com.example.blog.entity.Announcement;
import com.example.blog.service.AnnouncementService;
import com.example.blog.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 公告控制器
 * <p>
 * 路径约定：复数 /announcements 为公开只读接口（游客可访问），
 * /admin/announcement 为管理员接口（由 AdminInterceptor 校验权限）。
 * 控制器只负责接收参数与封装返回，业务规则统一放在 AnnouncementService。
 */
@RestController
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    /**
     * 公告列表（公开）
     * GET /announcements
     */
    @GetMapping("/announcements")
    public Result<List<Announcement>> listAnnouncements() {
        return Result.success(announcementService.listAll());
    }

    /**
     * 最新公告（公开，首页展示）
     * GET /announcements/latest?limit=1
     */
    @GetMapping("/announcements/latest")
    public Result<List<Announcement>> latestAnnouncements(
            @RequestParam(value = "limit", required = false) Integer limit) {
        return Result.success(announcementService.listLatest(limit));
    }

    /**
     * 新增公告（管理员）
     * POST /admin/announcement
     */
    @PostMapping("/admin/announcement")
    public Result<Announcement> addAnnouncement(@RequestBody AnnouncementDTO dto) {
        return Result.success("新增公告成功", announcementService.addAnnouncement(dto));
    }

    /**
     * 修改公告（管理员）
     * PUT /admin/announcement/{id}
     */
    @PutMapping("/admin/announcement/{id}")
    public Result<Announcement> updateAnnouncement(@PathVariable("id") Long id, @RequestBody AnnouncementDTO dto) {
        return Result.success("修改公告成功", announcementService.updateAnnouncement(id, dto));
    }

    /**
     * 删除公告（管理员）
     * DELETE /admin/announcement/{id}
     */
    @DeleteMapping("/admin/announcement/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable("id") Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success("删除公告成功");
    }
}
