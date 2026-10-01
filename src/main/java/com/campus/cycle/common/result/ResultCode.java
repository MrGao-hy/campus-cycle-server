package com.campus.cycle.common.result;

import lombok.Getter;

/**
 * 统一响应码
 * 对齐前端 src/api/request.ts 的约定：code === 200 视为业务成功，code === 401 触发登录拦截
 */
@Getter
public enum ResultCode {

    /** 业务成功 */
    SUCCESS(200, "success"),

    /** 参数校验失败 */
    PARAM_ERROR(400, "参数错误"),

    /** 未登录 / 登录过期 */
    UNAUTHORIZED(401, "请先登录"),

    /** 无权限操作 */
    FORBIDDEN(403, "无权限操作"),

    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),

    /** 业务规则不允许 */
    BUSINESS_ERROR(1000, "操作失败"),

    /** 服务器内部错误 */
    INTERNAL_ERROR(500, "服务器开小差了，请稍后重试");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
