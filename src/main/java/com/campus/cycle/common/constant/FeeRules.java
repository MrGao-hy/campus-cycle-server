package com.campus.cycle.common.constant;

import java.math.BigDecimal;

/**
 * 手续费规则：首单免费，成交价 6%，最低 1 元，最高 20 元
 */
public final class FeeRules {

    private FeeRules() {
    }

    public static final BigDecimal RATE = new BigDecimal("0.06");
    public static final BigDecimal MIN_AMOUNT = new BigDecimal("1");
    public static final BigDecimal MAX_AMOUNT = new BigDecimal("20");

    public static final String STATUS_UNPAID = "UNPAID";
    public static final String STATUS_PAID = "PAID";

    /** 首单免费 */
    public static final String FREE_REASON_FIRST = "首单免费";
}
