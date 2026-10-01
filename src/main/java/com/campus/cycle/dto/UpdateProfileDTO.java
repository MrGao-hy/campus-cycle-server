package com.campus.cycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人资料（微信授权头像昵称）
 */
@Data
@Schema(description = "更新个人资料请求")
public class UpdateProfileDTO {

    @Size(max = 20, message = "昵称最长 20 个字符")
    @Schema(description = "昵称（不传则不修改）")
    private String nickname;

    @Schema(description = "头像 URL（不传则不修改）")
    private String avatar;
}
