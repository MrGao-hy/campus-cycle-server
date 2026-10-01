package com.campus.cycle.vo;

import com.campus.cycle.entity.FeeBill;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 手续费账单汇总
 */
@Data
public class FeeSummaryVO {

    /** 未结清总金额 */
    private BigDecimal unpaidAmount;

    /** 未结清笔数 */
    private Integer unpaidCount;

    private List<FeeBill> bills;
}
