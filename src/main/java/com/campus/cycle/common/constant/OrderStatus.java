package com.campus.cycle.common.constant;

/**
 * 订单状态常量（六种固定状态，与前端类型定义一致）
 */
public final class OrderStatus {

    private OrderStatus() {
    }

    /** 待卖家确认 */
    public static final String PENDING_SELLER = "PENDING_SELLER";

    /** 待线下交易 */
    public static final String PENDING_OFFLINE = "PENDING_OFFLINE";

    /** 待买家确认 */
    public static final String PENDING_BUYER = "PENDING_BUYER";

    /** 申诉期 */
    public static final String APPEALING = "APPEALING";

    /** 已完成 */
    public static final String COMPLETED = "COMPLETED";

    /** 已取消 */
    public static final String CANCELLED = "CANCELLED";

    /** 卖家处理超时（24h） */
    public static final long SELLER_EXPIRE_MS = 24L * 60 * 60 * 1000;

    /** 申诉期（48h） */
    public static final long APPEAL_PERIOD_MS = 48L * 60 * 60 * 1000;
}
