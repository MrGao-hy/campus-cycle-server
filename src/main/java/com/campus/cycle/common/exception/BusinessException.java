package com.campus.cycle.common.exception;

import com.campus.cycle.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常：业务规则不允许的操作（如重复申请、状态不可流转、未结清手续费等）
 * 抛出的 message 会原样返回前端弹窗展示
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}
