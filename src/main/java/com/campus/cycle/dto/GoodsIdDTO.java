package com.campus.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 按商品 ID 操作（下架/重新上架/删除）
 */
@Data
public class GoodsIdDTO {

    @NotBlank(message = "商品 ID 不能为空")
    private String goodsId;
}
