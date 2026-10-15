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
                        "/static/**"
                );

        // 2. 注册管理员拦截器：仅拦截 /admin/** 路径，需管理员权限
        registry.addInterceptor(new AdminInterceptor())
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /upload/** 的访问映射到本地文件上传目录
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
