package com.example.blog.controller;

import com.example.blog.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试接口控制器
 * 用于验证框架是否正常运行
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 健康检查接口（无需登录即可访问）
     * 访问 GET /test/health
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("app", "blog-system");
        data.put("status", "ok");
        data.put("time", System.currentTimeMillis());
        return Result.success("请求成功", data);
    }
}
