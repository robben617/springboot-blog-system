package com.example.blog.config;

import com.example.blog.interceptor.AdminInterceptor;
import com.example.blog.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置
 * 注册登录拦截器、管理员拦截器，配置静态资源映射
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /** 文件上传存储根目录（来自 application.yml） */
    @Value("${file.upload-path}")
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 注册登录拦截器：拦截所有请求，排除登录、注册等公开接口
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/register",
                        "/test/**",         // 测试接口
                        "/error",
                        "/upload/**",       // 上传的静态资源（图片）
                        "/static/**",
                        // ===== 分类标签 / 评论模块的公开只读接口（游客可访问）=====
                        "/categories",      // 分类列表
                        "/categories/**",   // 按分类浏览文章（C3）
                        "/tags",            // 标签列表
                        "/tags/**",         // 按标签浏览文章（C4）
                        "/comments/**",     // 评论展示（M3）
                        // ===== 公告 / 友链模块的公开只读接口（游客可访问）=====
                        "/announcements",        // 公告列表
                        "/announcements/**",     // 最新公告（首页展示）
                        "/friend-links",         // 友情链接列表
                        "/friend-links/**",
                        // ===== 文章模块的公开只读接口（游客可访问）=====
                        "/articles",        // 文章分页列表 / 搜索
                        "/articles/**"      // 文章详情
                        // 注意：写操作使用单数路径 /article、/admin/**，必须登录，不要加入放行列表
                );

        // 2. 注册管理员拦截器：仅拦截 /admin/** 路径，需管理员权限
        registry.addInterceptor(new AdminInterceptor())
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /upload/** 的访问映射到本地文件上传目录
        // ⚠️ 注意：addResourceLocations 的路径必须以 "/" 结尾！
        // 若写成 file:./upload（缺尾斜杠），Spring 会把文件名错误拼接、定位不到资源，
        // 访问时抛 NoResourceFoundException 并返回 500，导致图片/静态文件全部打不开。
        // 这里统一兜底补上尾斜杠，无论配置写 ./upload 还是 ./upload/ 都能正常工作。
        String location = "file:" + uploadPath;
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(location);
    }
}
