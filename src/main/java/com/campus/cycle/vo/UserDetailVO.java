package com.campus.cycle.vo;

import com.campus.cycle.entity.Goods;
import lombok.Data;

import java.util.List;

/**
 * 用户主页（资料 + 在售商品 + 收到的评价）
 */
@Data
public class UserDetailVO {

    private UserProfileVO profile;

    /** 在售/锁定商品（可继续浏览） */
    private List<Goods> onSaleGoods;

    /** 收到的评价（作为卖家收到的买家评价） */
    private List<ReviewVO> reviews;
}
