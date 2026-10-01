package com.campus.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 买家提交购买申请请求
 */
@Data
public class ApplyBuyDTO {

    @NotBlank(message = "商品 ID 不能为空")
    private String goodsId;

    /** 买家申请留言 */
    private String remark;
}
