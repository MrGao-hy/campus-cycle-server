package com.campus.cycle.vo;

import com.campus.cycle.entity.Goods;
import lombok.Data;

/**
 * 订单 + 关联信息（列表/详情联查视图，对齐前端 OrderRow 结构）
 */
@Data
public class OrderRowVO {

    private OrderVO order;
    private Goods goods;
    private UserProfileVO buyer;
    private UserProfileVO seller;
}
