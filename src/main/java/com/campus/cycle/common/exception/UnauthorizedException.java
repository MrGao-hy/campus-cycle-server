package com.campus.cycle.common.exception;

/**
 * 未登录 / 登录过期异常（由认证拦截器抛出）
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("请先登录");
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
