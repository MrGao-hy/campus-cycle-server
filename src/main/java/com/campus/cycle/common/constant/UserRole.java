package com.campus.cycle.common.constant;

/**
 * 用户角色常量（t_user.role）
 */
public final class UserRole {

    private UserRole() {
    }

    /** 普通用户 */
    public static final String USER = "USER";

    /** 管理员（可访问后台审核接口） */
    public static final String ADMIN = "ADMIN";
}
