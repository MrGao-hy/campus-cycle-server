package com.campus.cycle.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人资料（微信授权头像昵称、联系方式）
 */
@Data
@Schema(description = "更新个人资料请求")
public class UpdateProfileDTO {

    @Size(max = 20, message = "昵称最长 20 个字符")
    @Schema(description = "昵称（不传则不修改）")
    private String nickname;

    @Schema(description = "头像 URL（不传则不修改）")
    private String avatar;

    @Size(max = 20, message = "联系电话最长 20 个字符")
    @Schema(description = "联系电话（不传则不修改，传空串可清空）")
    private String contactPhone;

    @Size(max = 32, message = "QQ 号最长 32 个字符")
    @Schema(description = "QQ 号（不传则不修改，传空串可清空）")
    private String contactQq;

    @Size(max = 50, message = "微信号最长 50 个字符")
    @Schema(description = "微信号（不传则不修改，传空串可清空）")
    private String contactWechat;

    @Size(max = 100, message = "邮箱最长 100 个字符")
    @Schema(description = "邮箱（不传则不修改，传空串可清空）")
    private String contactEmail;
}
