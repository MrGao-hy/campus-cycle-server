package com.campus.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单操作请求（卖家确认/拒绝/交付、买家确认、卖家申诉等仅需 orderId 的操作通用）
 */
@Data
public class OrderActionDTO {

    @NotBlank(message = "订单 ID 不能为空")
    private String orderId;
}
