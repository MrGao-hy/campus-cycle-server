package com.campus.cycle.security;

import com.campus.cycle.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 认证拦截器
 * 从请求头 token 解析用户 ID 写入 UserContext；无效 token 抛未登录异常
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String TOKEN_HEADER = "token";

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String token = request.getHeader(TOKEN_HEADER);
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException();
        }
        String userId = jwtUtil.parseUserId(token);
        if (userId == null) {
            throw new UnauthorizedException("登录已过期，请重新登录");
        }
        UserContext.setUserId(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束必须清理，防止线程复用导致串号
        UserContext.clear();
    }
}
