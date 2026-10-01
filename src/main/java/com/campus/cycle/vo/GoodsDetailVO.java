package com.campus.cycle.vo;

import com.campus.cycle.entity.Goods;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 商品详情（商品 + 卖家信息 + 评价列表）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GoodsDetailVO extends Goods {

    private UserProfileVO seller;

    private List<ReviewVO> reviews;
}
