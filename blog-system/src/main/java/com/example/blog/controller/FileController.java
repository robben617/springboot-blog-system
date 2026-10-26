package com.example.blog.controller;

import com.example.blog.util.FileUploadUtil;
import com.example.blog.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;

/**
 * 文件上传控制器（需登录）
 * <p>
 * 路径约定：/article/** 属于登录后接口（由 LoginInterceptor 拦截，不在放行白名单），
 * 因此封面上传要求登录，游客无法调用。
 * <p>
 * 说明：头像上传接口 POST /user/avatar 已在用户模块实现，此处不重复；
 * 本控制器只提供文章封面上传（对应文件模块 F2）。
 */
@RestController
@RequiredArgsConstructor
public class FileController {

    /** 文件上传存储根目录（来自 application.yml 的 file.upload-path） */
    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 上传文章封面图（需登录）
     * POST /article/cover
     * form-data：file=封面图片（jpg/jpeg/png/gif/webp，不超过 5MB）
     * <p>
     * 返回可直接访问、也可回填到文章 cover 字段的相对路径：
     * /upload/yyyy/MM/dd/xxxxxxxxxxxxxxxx.jpg
     */
    @PostMapping("/article/cover")
    public Result<String> uploadCover(@RequestParam("file") MultipartFile file) {
        // 必须把相对路径（./upload）解析成绝对路径再传入工具类：
        // Spring Boot 3（Servlet 6）下 MultipartFile#transferTo 遇到相对路径会按
        // 容器 multipart 临时目录解析，导致「文件保存失败」（与头像上传相同的处理）。
        String absoluteUploadPath = Paths.get(uploadPath).toAbsolutePath().normalize().toString();
        String url = FileUploadUtil.uploadCover(file, absoluteUploadPath);
        return Result.success("封面上传成功", url);
    }
}
