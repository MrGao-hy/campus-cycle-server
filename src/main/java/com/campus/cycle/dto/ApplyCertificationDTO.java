package com.campus.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 提交校园认证申请请求（首次申请或驳回后重新提交）
 */
@Data
public class ApplyCertificationDTO {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名过长")
    private String realName;

    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "MALE|FEMALE", message = "性别取值为 MALE 或 FEMALE")
    private String gender;

    @NotBlank(message = "学号不能为空")
    @Size(max = 32, message = "学号过长")
    private String studentNo;

    @NotBlank(message = "请上传学生证照片")
    @Size(max = 512, message = "学生证照片 URL 过长")
    private String studentCardImage;
}
