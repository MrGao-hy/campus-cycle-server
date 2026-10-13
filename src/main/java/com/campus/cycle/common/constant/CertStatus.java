package com.campus.cycle.common.constant;

/**
 * 校园认证状态常量（NONE 仅作查询态，不落库）
 */
public final class CertStatus {

    private CertStatus() {
    }

    /** 未申请（仅查询返回，不落库） */
    public static final String NONE = "NONE";

    /** 待审核 */
    public static final String PENDING = "PENDING";

    /** 已通过 */
    public static final String APPROVED = "APPROVED";

    /** 已驳回（可重新提交） */
    public static final String REJECTED = "REJECTED";
}
