package com.campus.cycle.service;

import com.campus.cycle.dto.CancelOrderDTO;
import com.campus.cycle.dto.SubmitReviewDTO;
import com.campus.cycle.vo.OrderRowVO;

import java.util.List;

/**
 * 订单服务（核心状态机）
 */
public interface OrderService {

    /** 订单列表（按角色 + 状态筛选，查询前先执行超时/申诉期自动流转） */
    List<OrderRowVO> list(String role, String status);

    /** 订单详情（含商品/买卖双方/评价） */
    OrderRowVO detail(String orderId);

    /** 卖家确认卖给该买家 → 待线下交易，展示联系方式 */
    void sellerConfirm(String orderId);

    /** 卖家拒绝申请（商品继续在售） */
    void sellerReject(String orderId);

    /** 卖家标记已当面交付 → 提醒买家确认 */
    void sellerDelivered(String orderId);

    /** 买家确认已完成 → 进入 48 小时申诉期 */
    void buyerConfirm(String orderId);

    /** 卖家在申诉期提出异议 → 平台申诉处理 */
    void sellerObjection(String orderId);

    /** 取消订单（双方未交易成功均可取消，未进入线下交易阶段时商品恢复在售） */
    void cancel(CancelOrderDTO dto);

    /** 买家评价 */
    void submitReview(SubmitReviewDTO dto);

    /**
     * 订单状态自动流转（定时任务与查询前触发）：
     * 1. 待卖家确认超时 → 自动过期取消，不收手续费
     * 2. 申诉期到期且卖家无异议 → 自动完成、商品置灰、生成手续费账单
     *
     * @return 本次流转的订单数
     */
    int sweepOrders();
}
