package com.example.blog.interceptor;

import com.example.blog.common.Constants;
import com.example.blog.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 * 基于Session校验用户登录状态，未登录返回401
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行CORS预检请求（OPTIONS），否则跨域时前端会被拦截
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // 从Session中获取用户ID
        Object userId = request.getSession().getAttribute(Constants.SESSION_USER_ID);
        if (userId == null) {
            log.warn("未登录访问: {} {}", method, request.getRequestURI());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "未登录，请先登录")));
            return false;
        }

        // 已登录，将用户信息存入请求属性，方便后续Controller直接获取
        request.setAttribute("userId", userId);
        request.setAttribute("username", request.getSession().getAttribute(Constants.SESSION_USERNAME));
        request.setAttribute("role", request.getSession().getAttribute(Constants.SESSION_ROLE));
        return true;
    }
}
