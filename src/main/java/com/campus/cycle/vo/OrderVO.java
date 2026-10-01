package com.campus.cycle.vo;

import com.campus.cycle.entity.Order;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 订单视图（订单 + 内嵌买家评价，对齐前端 order.review 结构）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVO extends Order {

    /** 买家评价 */
    private ReviewVO review;
}
