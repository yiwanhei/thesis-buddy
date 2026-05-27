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
        "/api/admin/auth/login"
    };

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String uri = req.getRequestURI();

        // 只拦截 /api/admin/ 开头的请求
        if (!uri.startsWith("/api/admin/")) {
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

        // 将adminId设置到请求属性中
        Integer adminId = jwtUtil.getAdminId(token);
        req.setAttribute("adminId", adminId);

        chain.doFilter(request, response);
    }
}
