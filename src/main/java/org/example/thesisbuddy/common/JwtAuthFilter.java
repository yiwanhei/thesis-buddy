package org.example.thesisbuddy.common;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class JwtAuthFilter implements Filter {

    @Autowired
    private JwtUtil jwtUtil;

    // 不需要认证的路径
    private static final String[] EXCLUDED_PATHS = {
        "/api/admin/auth/login",
        "/api/teacher/auth/login"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        // 只拦截 /api/admin/ 和 /api/teacher/ 开头的请求
        if (!uri.startsWith("/api/admin/") && !uri.startsWith("/api/teacher/")) {
            chain.doFilter(request, response);
            return;
        }

        // 排除登录接口
        for (String path : EXCLUDED_PATHS) {
            if (uri.equals(path)) {
                chain.doFilter(request, response);
                return;
            }
        }

        // 检查Authorization头
        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            res.setStatus(401);
            res.setContentType("application/json;charset=utf-8");
            res.getWriter().write("{\"code\":401,\"msg\":\"未登录或Token已过期\"}");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            res.setStatus(401);
            res.setContentType("application/json;charset=utf-8");
            res.getWriter().write("{\"code\":401,\"msg\":\"Token无效或已过期\"}");
            return;
        }

        // 根据路径设置对应的用户ID到请求属性中
        if (uri.startsWith("/api/admin/")) {
            Integer adminId = jwtUtil.getAdminId(token);
            req.setAttribute("adminId", adminId);
        } else if (uri.startsWith("/api/teacher/")) {
            Integer teacherId = jwtUtil.getTeacherId(token);
            req.setAttribute("teacherId", teacherId);
        }

        chain.doFilter(request, response);
    }
}
