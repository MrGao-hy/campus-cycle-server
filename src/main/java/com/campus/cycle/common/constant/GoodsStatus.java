package com.campus.cycle.common.constant;

/**
 * 商品状态常量
 */
public final class GoodsStatus {

    private GoodsStatus() {
    }

    /** 在售 */
    public static final String ON_SALE = "ON_SALE";

    /** 交易进行中（已锁定） */
    public static final String LOCKED = "LOCKED";

    /** 已售出 */
    public static final String SOLD = "SOLD";
}
