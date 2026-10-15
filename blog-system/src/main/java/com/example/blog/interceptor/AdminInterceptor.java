package com.example.blog.interceptor;

import com.example.blog.common.Constants;
import com.example.blog.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员权限拦截器
 * 校验当前登录用户是否为管理员，非管理员返回403
 */
public class AdminInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从 Session 中获取用户角色
        Object role = request.getSession().getAttribute(Constants.SESSION_ROLE);
        if (role == null || (Integer) role != Constants.ROLE_ADMIN) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(403, "无权限，需要管理员身份")));
            return false;
        }
        return true;
    }
}
