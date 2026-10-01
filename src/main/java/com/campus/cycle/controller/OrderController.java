package com.campus.cycle.controller;

import com.campus.cycle.common.result.Result;
import com.campus.cycle.dto.CancelOrderDTO;
import com.campus.cycle.dto.OrderActionDTO;
import com.campus.cycle.dto.SubmitReviewDTO;
import com.campus.cycle.service.OrderService;
import com.campus.cycle.vo.OrderRowVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 订单接口（核心状态机）
 */
@Tag(name = "订单")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "订单列表（按角色 + 状态筛选）")
    @GetMapping("/list")
    public Result<List<OrderRowVO>> list(@RequestParam String role,
                                         @RequestParam(defaultValue = "ALL") String status) {
        return Result.success(orderService.list(role, status));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/detail/{id}")
    public Result<OrderRowVO> detail(@PathVariable String id) {
        return Result.success(orderService.detail(id));
    }

    @Operation(summary = "卖家确认卖给该买家 → 待线下交易")
    @PostMapping("/seller-confirm")
    public Result<Void> sellerConfirm(@Valid @RequestBody OrderActionDTO dto) {
        orderService.sellerConfirm(dto.getOrderId());
        return Result.success();
    }

    @Operation(summary = "卖家拒绝申请（商品继续在售）")
    @PostMapping("/seller-reject")
    public Result<Void> sellerReject(@Valid @RequestBody OrderActionDTO dto) {
        orderService.sellerReject(dto.getOrderId());
        return Result.success();
    }

    @Operation(summary = "卖家标记已当面交付 → 提醒买家确认")
    @PostMapping("/seller-delivered")
    public Result<Void> sellerDelivered(@Valid @RequestBody OrderActionDTO dto) {
        orderService.sellerDelivered(dto.getOrderId());
        return Result.success();
    }

    @Operation(summary = "买家确认已完成 → 进入 48 小时申诉期")
    @PostMapping("/buyer-confirm")
    public Result<Void> buyerConfirm(@Valid @RequestBody OrderActionDTO dto) {
        orderService.buyerConfirm(dto.getOrderId());
        return Result.success();
    }

    @Operation(summary = "卖家在申诉期提出异议 → 平台申诉处理")
    @PostMapping("/seller-objection")
    public Result<Void> sellerObjection(@Valid @RequestBody OrderActionDTO dto) {
        orderService.sellerObjection(dto.getOrderId());
        return Result.success();
    }

    @Operation(summary = "取消订单（双方未交易成功均可取消）")
    @PostMapping("/cancel")
    public Result<Void> cancel(@Valid @RequestBody CancelOrderDTO dto) {
        orderService.cancel(dto);
        return Result.success();
    }

    @Operation(summary = "买家评价")
    @PostMapping("/review")
    public Result<Void> submitReview(@Valid @RequestBody SubmitReviewDTO dto) {
        orderService.submitReview(dto);
        return Result.success();
    }
}
