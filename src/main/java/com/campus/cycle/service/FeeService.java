package com.campus.cycle.service;

import com.campus.cycle.entity.FeeBill;
import com.campus.cycle.vo.CheckPublishVO;
import com.campus.cycle.vo.FeeSummaryVO;

import java.util.List;

/**
 * 手续费服务
 */
public interface FeeService {

    /** 手续费账单汇总（未结清禁止发布新商品） */
    FeeSummaryVO summary();

    /** 账单列表（按生成时间倒序） */
    List<FeeBill> bills();

    /** 支付手续费 */
    void pay(String billId);

    /** 是否允许发布新商品 */
    CheckPublishVO checkPublish();
}
