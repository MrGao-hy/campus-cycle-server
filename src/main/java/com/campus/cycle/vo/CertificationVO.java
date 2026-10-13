package com.campus.cycle.vo;

import lombok.Data;

import java.util.Date;

/**
 * 校园认证信息（用户端状态查询 + 后台审核列表共用）
 */
@Data
public class CertificationVO {

    private String id;

    /** 申请用户 ID */
    private String userId;

    /** 申请人昵称（后台列表联查填充，用户端查询为 null） */
    private String nickname;

    /** 真实姓名 */
    private String realName;

    /** 性别 MALE-男 FEMALE-女 */
    private String gender;

    /** 学号 */
    private String studentNo;

    /** 学生证照片 URL */
    private String studentCardImage;

    /** 状态 NONE-未申请 PENDING-待审核 APPROVED-已通过 REJECTED-已驳回 */
    private String status;

    /** 驳回原因（已驳回时返回，供前端展示） */
    private String rejectReason;

    /** 申请时间（Jackson 全局 date-format 输出 yyyy-MM-dd HH:mm:ss） */
    private Date applyTime;

    /** 审核时间（Jackson 全局 date-format 输出 yyyy-MM-dd HH:mm:ss） */
    private Date reviewTime;
}
