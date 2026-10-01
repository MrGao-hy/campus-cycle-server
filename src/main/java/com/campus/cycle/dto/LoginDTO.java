package com.campus.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信登录请求
 */
@Data
public class LoginDTO {

    /** 微信官方返回的用户登录凭证 */
    @NotBlank(message = "jsCode 不能为空")
    private String jsCode;
}
