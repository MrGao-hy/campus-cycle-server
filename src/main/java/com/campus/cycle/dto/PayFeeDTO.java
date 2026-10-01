package com.campus.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 支付手续费请求
 */
@Data
public class PayFeeDTO {

    @NotBlank(message = "账单 ID 不能为空")
    private String billId;
}
