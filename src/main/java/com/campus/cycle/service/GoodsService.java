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

    /** 本校商品列表（分页；已售出置灰展示、不隐藏） */
    PageResult<Goods> list(String schoolId, String keyword, String category, long pageNum, long pageSize);

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
}
