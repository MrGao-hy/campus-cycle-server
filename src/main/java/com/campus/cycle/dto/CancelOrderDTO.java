package com.campus.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 取消订单请求
 */
@Data
public class CancelOrderDTO {

    @NotBlank(message = "订单 ID 不能为空")
    private String orderId;

    @NotBlank(message = "取消原因不能为空")
    @Size(max = 200, message = "取消原因过长")
    private String reason;
}
