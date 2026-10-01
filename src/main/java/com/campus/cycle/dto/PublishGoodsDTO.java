package com.campus.cycle.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 发布商品请求
 */
@Data
public class PublishGoodsDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题过长")
    private String title;

    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    private BigDecimal price;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotBlank(message = "成色不能为空")
    private String condition;

    @NotBlank(message = "描述不能为空")
    @Size(max = 2000, message = "描述过长")
    private String description;

    /** 图片 URL 数组 */
    private List<String> images;
}
