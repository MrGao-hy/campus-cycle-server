package com.campus.cycle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.cycle.common.constant.FeeRules;
import com.campus.cycle.common.constant.GoodsStatus;
import com.campus.cycle.common.constant.OrderStatus;
import com.campus.cycle.common.exception.BusinessException;
import com.campus.cycle.common.result.ResultCode;
import com.campus.cycle.common.util.Assemblers;
import com.campus.cycle.dto.CancelOrderDTO;
import com.campus.cycle.dto.SubmitReviewDTO;
import com.campus.cycle.entity.FeeBill;
import com.campus.cycle.entity.Goods;
import com.campus.cycle.entity.Order;
import com.campus.cycle.entity.Review;
import com.campus.cycle.entity.User;
import com.campus.cycle.mapper.FeeBillMapper;
import com.campus.cycle.mapper.GoodsMapper;
import com.campus.cycle.mapper.OrderMapper;
import com.campus.cycle.mapper.ReviewMapper;
import com.campus.cycle.mapper.UserMapper;
import com.campus.cycle.security.UserContext;
import com.campus.cycle.service.OrderService;
import com.campus.cycle.service.SchoolService;
import com.campus.cycle.vo.OrderRowVO;
import com.campus.cycle.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单服务（核心状态机）
 * 状态流转：PENDING_SELLER → PENDING_OFFLINE → PENDING_BUYER → APPEALING → COMPLETED / CANCELLED
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final ReviewMapper reviewMapper;
    private final FeeBillMapper feeBillMapper;
    private final SchoolService schoolService;

    @Override
    public List<OrderRowVO> list(String role, String status) {
        sweepOrders();
        String uid = UserContext.requireUserId();
        var query = Wrappers.<Order>lambdaQuery()
                .eq("buyer".equals(role) ? Order::getBuyerId : Order::getSellerId, uid);
        if (status != null && !"ALL".equals(status)) {
            query.eq(Order::getStatus, status);
        }
        query.orderByDesc(Order::getApplyTime);
        return orderMapper.selectList(query).stream()
                .map(o -> toRow(o.getId()))
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public OrderRowVO detail(String orderId) {
        // 越权校验：仅订单买卖双方可查看（订单含双方联系方式等敏感信息）
        String uid = UserContext.requireUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!uid.equals(order.getBuyerId()) && !uid.equals(order.getSellerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看该订单");
        }
        OrderRowVO row = toRow(orderId);
        if (row == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        return row;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sellerConfirm(String orderId) {
        Order order = requireOrder(orderId);
        requireSeller(order);
        if (!OrderStatus.PENDING_SELLER.equals(order.getStatus())) {
            throw new BusinessException("当前状态不可确认");
        }
        order.setStatus(OrderStatus.PENDING_OFFLINE);
        order.setSellerConfirmTime(System.currentTimeMillis());
        // 商品锁定
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null && GoodsStatus.ON_SALE.equals(goods.getStatus())) {
            goods.setStatus(GoodsStatus.LOCKED);
            goodsMapper.updateById(goods);
        }
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sellerReject(String orderId) {
        Order order = requireOrder(orderId);
        requireSeller(order);
        if (!OrderStatus.PENDING_SELLER.equals(order.getStatus())) {
            throw new BusinessException("当前状态不可操作");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(System.currentTimeMillis());
        order.setCancelReason("卖家拒绝了该购买申请");
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sellerDelivered(String orderId) {
        Order order = requireOrder(orderId);
        requireSeller(order);
        if (!OrderStatus.PENDING_OFFLINE.equals(order.getStatus())) {
            throw new BusinessException("当前状态不可操作");
        }
        order.setStatus(OrderStatus.PENDING_BUYER);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buyerConfirm(String orderId) {
        Order order = requireOrder(orderId);
        requireBuyer(order);
        if (!OrderStatus.PENDING_OFFLINE.equals(order.getStatus())
                && !OrderStatus.PENDING_BUYER.equals(order.getStatus())) {
            throw new BusinessException("当前状态不可确认");
        }
        long now = System.currentTimeMillis();
        order.setStatus(OrderStatus.APPEALING);
        order.setBuyerConfirmTime(now);
        order.setAppealEndTime(now + OrderStatus.APPEAL_PERIOD_MS);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sellerObjection(String orderId) {
        Order order = requireOrder(orderId);
        requireSeller(order);
        if (!OrderStatus.APPEALING.equals(order.getStatus())) {
            throw new BusinessException("当前状态不可操作");
        }
        order.setSellerObjection(true);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(CancelOrderDTO dto) {
        Order order = requireOrder(dto.getOrderId());
        String uid = UserContext.requireUserId();
        if (!uid.equals(order.getBuyerId()) && !uid.equals(order.getSellerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权操作该订单");
        }
        if (OrderStatus.COMPLETED.equals(order.getStatus()) || OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new BusinessException("当前状态不可取消");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelTime(System.currentTimeMillis());
        order.setCancelReason(dto.getReason());
        orderMapper.updateById(order);

        // 仅在从未进入线下交易阶段时释放商品；已锁定商品在申诉期结束后由平台处理
        if (order.getBuyerConfirmTime() == null) {
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            if (goods != null && GoodsStatus.LOCKED.equals(goods.getStatus())) {
                boolean hasActive = orderMapper.selectCount(Wrappers.<Order>lambdaQuery()
                        .eq(Order::getGoodsId, order.getGoodsId())
                        .ne(Order::getId, order.getId())
                        .in(Order::getStatus,
                                OrderStatus.PENDING_OFFLINE, OrderStatus.PENDING_BUYER, OrderStatus.APPEALING)) > 0;
                if (!hasActive) {
                    goods.setStatus(GoodsStatus.ON_SALE);
                    goodsMapper.updateById(goods);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitReview(SubmitReviewDTO dto) {
        Order order = requireOrder(dto.getOrderId());
        requireBuyer(order);
        if (!OrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new BusinessException("订单完成后才能评价");
        }
        Long count = reviewMapper.selectCount(Wrappers.<Review>lambdaQuery()
                .eq(Review::getOrderId, order.getId()));
        if (count > 0) {
            throw new BusinessException("该订单已评价");
        }
        User user = userMapper.selectById(UserContext.requireUserId());
        Review review = new Review();
        review.setGoodsId(order.getGoodsId());
        review.setOrderId(order.getId());
        review.setFromUserId(order.getBuyerId());
        review.setFromNickname(user != null ? user.getNickname() : "");
        review.setRate(dto.getRate());
        review.setContent(dto.getContent());
        review.setTime(System.currentTimeMillis());
        reviewMapper.insert(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int sweepOrders() {
        long ts = System.currentTimeMillis();
        int count = 0;
        // 1. 待卖家确认超时 → 自动过期取消，不收手续费
        List<Order> expired = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
                .eq(Order::getStatus, OrderStatus.PENDING_SELLER)
                .lt(Order::getExpireTime, ts));
        for (Order order : expired) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelTime(order.getExpireTime());
            order.setCancelReason("卖家超时未确认，订单自动过期（不收手续费）");
            orderMapper.updateById(order);
            // 商品恢复在售
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            if (goods != null && GoodsStatus.LOCKED.equals(goods.getStatus())) {
                goods.setStatus(GoodsStatus.ON_SALE);
                goodsMapper.updateById(goods);
            }
            count++;
        }
        // 2. 申诉期到期且卖家无异议 → 自动完成、商品置灰、生成手续费账单
        List<Order> appealExpired = orderMapper.selectList(Wrappers.<Order>lambdaQuery()
                .eq(Order::getStatus, OrderStatus.APPEALING)
                .eq(Order::getSellerObjection, false)
                .isNotNull(Order::getAppealEndTime)
                .lt(Order::getAppealEndTime, ts));
        for (Order order : appealExpired) {
            completeOrder(order);
            count++;
        }
        if (count > 0) {
            log.info("订单自动流转 {} 笔", count);
        }
        return count;
    }

    /** 订单最终完成：商品置灰（不隐藏）、生成手续费账单 */
    private void completeOrder(Order order) {
        long ts = System.currentTimeMillis();
        order.setStatus(OrderStatus.COMPLETED);
        order.setCompleteTime(ts);
        if (order.getAppealEndTime() == null) {
            order.setAppealEndTime(ts);
        }
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            goods.setStatus(GoodsStatus.SOLD);
            goodsMapper.updateById(goods);
        }
        // 手续费只在订单最终完成后生成
        if (Boolean.FALSE.equals(order.getFeeBilled())) {
            User seller = userMapper.selectById(order.getSellerId());
            if (seller != null && goods != null) {
                boolean isFirst = seller.getSuccessCount() == null || seller.getSuccessCount() == 0;
                FeeBill bill = computeFee(order, goods, seller, isFirst);
                seller.setSuccessCount((seller.getSuccessCount() == null ? 0 : seller.getSuccessCount()) + 1);
                userMapper.updateById(seller);
                feeBillMapper.insert(bill);
                order.setFeeBilled(true);
            }
        }
        orderMapper.updateById(order);
    }

    /** 手续费计算：首单免费，成交价 6%，最低 1 元，最高 20 元 */
    private FeeBill computeFee(Order order, Goods goods, User seller, boolean isFirst) {
        long ts = System.currentTimeMillis();
        BigDecimal amount;
        String freeReason = null;
        if (isFirst) {
            amount = BigDecimal.ZERO;
            freeReason = "首笔成功交易免手续费";
        } else {
            BigDecimal raw = order.getPrice().multiply(FeeRules.RATE).setScale(2, java.math.RoundingMode.HALF_UP);
            amount = raw.min(FeeRules.MAX_AMOUNT).max(FeeRules.MIN_AMOUNT);
        }
        FeeBill bill = new FeeBill();
        bill.setOrderId(order.getId());
        bill.setSellerId(order.getSellerId());
        bill.setGoodsTitle(goods.getTitle());
        bill.setDealPrice(order.getPrice());
        bill.setRate(FeeRules.RATE);
        bill.setAmount(amount);
        bill.setFreeReason(freeReason);
        bill.setStatus(amount.compareTo(BigDecimal.ZERO) == 0 ? FeeRules.STATUS_PAID : FeeRules.STATUS_UNPAID);
        bill.setCreateTime(ts);
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            bill.setPayTime(ts);
        }
        return bill;
    }

    /** 订单联查视图：订单 + 商品 + 买卖双方 + 买家评价 */
    private OrderRowVO toRow(String orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return null;
        }
        Goods goods = goodsMapper.selectById(order.getGoodsId());
        User buyer = userMapper.selectById(order.getBuyerId());
        User seller = userMapper.selectById(order.getSellerId());
        if (goods == null || buyer == null || seller == null) {
            return null;
        }
        OrderRowVO row = new OrderRowVO();
        OrderVO orderVO = new OrderVO();
        orderVO.setId(order.getId());
        orderVO.setGoodsId(order.getGoodsId());
        orderVO.setBuyerId(order.getBuyerId());
        orderVO.setSellerId(order.getSellerId());
        orderVO.setStatus(order.getStatus());
        orderVO.setPrice(order.getPrice());
        orderVO.setApplyTime(order.getApplyTime());
        orderVO.setExpireTime(order.getExpireTime());
        orderVO.setSellerConfirmTime(order.getSellerConfirmTime());
        orderVO.setBuyerConfirmTime(order.getBuyerConfirmTime());
        orderVO.setAppealEndTime(order.getAppealEndTime());
        orderVO.setSellerObjection(order.getSellerObjection());
        orderVO.setCancelTime(order.getCancelTime());
        orderVO.setCancelReason(order.getCancelReason());
        orderVO.setCompleteTime(order.getCompleteTime());
        orderVO.setFeeBilled(order.getFeeBilled());
        orderVO.setRemark(order.getRemark());
        Review review = reviewMapper.selectOne(Wrappers.<Review>lambdaQuery().eq(Review::getOrderId, orderId));
        if (review != null) {
            orderVO.setReview(Assemblers.toReviewVO(review));
        }
        row.setOrder(orderVO);
        row.setGoods(goods);
        row.setBuyer(Assemblers.toProfile(buyer, schoolService.nameOf(buyer.getSchoolId())));
        row.setSeller(Assemblers.toProfile(seller, schoolService.nameOf(seller.getSchoolId())));
        return row;
    }

    private Order requireOrder(String orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private void requireSeller(Order order) {
        String uid = UserContext.requireUserId();
        if (!uid.equals(order.getSellerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅卖家可操作");
        }
    }

    private void requireBuyer(Order order) {
        String uid = UserContext.requireUserId();
        if (!uid.equals(order.getBuyerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "仅买家可操作");
        }
    }
}
