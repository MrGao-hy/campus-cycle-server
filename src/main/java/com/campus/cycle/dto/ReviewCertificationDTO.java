package com.campus.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台审核认证申请请求（通过 / 不通过）
 */
@Data
public class ReviewCertificationDTO {

    @NotBlank(message = "认证申请 ID 不能为空")
    private String id;

    /** true-通过 false-不通过 */
    @NotNull(message = "审核结果不能为空")
    private Boolean approve;

    /** 驳回原因（不通过时必填） */
    @Size(max = 200, message = "驳回原因过长")
    private String reason;
}
