package com.campus.cycle.vo;

import lombok.Data;

/**
 * 商品评价（对齐前端 GoodsReview 结构）
 */
@Data
public class ReviewVO {

    private String id;
    private String goodsId;
    private String orderId;
    private String fromUserId;
    private String fromNickname;
    private Integer rate;
    private String content;
    private Long time;
}
