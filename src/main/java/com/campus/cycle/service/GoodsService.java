package com.campus.cycle.service;

import com.campus.cycle.dto.ApplyBuyDTO;
import com.campus.cycle.dto.PublishGoodsDTO;
import com.campus.cycle.entity.Goods;
import com.campus.cycle.vo.GoodsDetailVO;
import com.campus.cycle.vo.PageResult;

import java.util.List;

/**
 * 商品服务
 */
public interface GoodsService {

    /**
     * 本校商品列表（分页；已售出置灰展示、不隐藏）。
     * 不传 schoolId：从当前登录用户绑定的学校取（登录时绑定学校）。
     */
    PageResult<Goods> list(String keyword, String category, long pageNum, long pageSize);

    /** 商品详情（含卖家信息与评价） */
    GoodsDetailVO detail(String id);

    /** 我发布的商品 */
    List<Goods> mine();

    /** 发布商品（卖家有未结清手续费时禁止） */
    Goods publish(PublishGoodsDTO dto);

    /** 买家提交购买申请（创建订单：待卖家确认），返回订单 ID */
    String applyBuy(ApplyBuyDTO dto);

    /** 买家发起站内沟通（不存在则创建会话），返回会话 ID */
    String startConversation(String goodsId);

    /** 卖家下架商品（仅自己在售商品可下架，下架后买家不可见） */
    void offShelf(String goodsId);

    /** 卖家重新上架（已下架商品恢复在售） */
    void onShelf(String goodsId);

    /** 卖家删除商品（未售出的商品才可删除，逻辑删除） */
    void delete(String goodsId);
}
