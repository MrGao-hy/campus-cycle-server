package com.campus.cycle.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 是否允许发布新商品（未结清手续费时禁止）
 */
@Data
@AllArgsConstructor
public class CheckPublishVO {

    private Boolean allowed;
    private BigDecimal unpaidAmount;
}
