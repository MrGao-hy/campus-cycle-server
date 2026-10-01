package com.campus.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 确认学校请求
 */
@Data
public class ConfirmSchoolDTO {

    @NotBlank(message = "学校 ID 不能为空")
    private String schoolId;
}
