package com.campus.cycle.common.constant;

/**
 * 信用分规则：初始 60 分，每成交一次 +5 分，上限 100 分
 */
public final class CreditRules {

    private CreditRules() {
    }

    /** 注册初始信用分 */
    public static final int INITIAL_SCORE = 60;

    /** 每次成功交易加分 */
    public static final int DEAL_REWARD = 5;

    /** 信用分上限 */
    public static final int MAX_SCORE = 100;
}
