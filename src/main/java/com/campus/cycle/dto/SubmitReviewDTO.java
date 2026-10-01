package com.campus.cycle.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 买家提交评价请求
 */
@Data
public class SubmitReviewDTO {

    @NotBlank(message = "订单 ID 不能为空")
    private String orderId;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分范围 1-5")
    @Max(value = 5, message = "评分范围 1-5")
    private Integer rate;

    @NotBlank(message = "评价内容不能为空")
    @Size(max = 500, message = "评价内容过长")
    private String content;
}
