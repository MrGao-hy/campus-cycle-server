package com.campus.cycle.config;

import com.campus.cycle.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时自动流转定时任务：
 * 1. 待卖家确认 24h 未处理 → 自动过期取消（不收手续费）
 * 2. 申诉期 48h 到期且卖家无异议 → 自动完成、商品置灰、生成手续费账单
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduler {

    private final OrderService orderService;

    /** 默认每分钟扫描一次 */
    @Scheduled(fixedDelayString = "${campus.scheduler.order-sweep-interval-ms:60000}")
    public void sweepOrders() {
        try {
            orderService.sweepOrders();
        } catch (Exception e) {
            log.error("订单自动流转执行失败", e);
        }
    }
}
