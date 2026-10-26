package com.example.blog.controller;

import com.example.blog.service.AdminStatsService;
import com.example.blog.util.Result;
import com.example.blog.vo.AdminStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理概览统计控制器
 * <p>
 * 路径以 /admin 开头，由 AdminInterceptor 统一校验管理员权限（role=1）。
 */
@RestController
@RequiredArgsConstructor
public class AdminStatsController {

    private final AdminStatsService adminStatsService;

    /**
     * 后台概览统计
     * GET /admin/stats
     */
    @GetMapping("/admin/stats")
    public Result<AdminStatsVO> getStats() {
        return Result.success(adminStatsService.getStats());
    }
}
