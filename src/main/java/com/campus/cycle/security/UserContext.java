package com.campus.cycle.security;

/**
 * 当前登录用户上下文（ThreadLocal）
 * 认证拦截器解析 token 后写入，请求结束时清除
 */
public final class UserContext {

    private static final ThreadLocal<String> CURRENT_USER_ID = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUserId(String userId) {
        CURRENT_USER_ID.set(userId);
    }

    /** 当前登录用户 ID；未登录时为 null */
    public static String getUserId() {
        return CURRENT_USER_ID.get();
    }

    /** 获取当前用户 ID，未登录直接抛异常（仅限已认证接口内使用） */
    public static String requireUserId() {
        String userId = CURRENT_USER_ID.get();
        if (userId == null) {
            throw new com.campus.cycle.common.exception.UnauthorizedException();
        }
        return userId;
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}
