package com.campus.cycle.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 校园认证（一人一条记录，驳回后可重新提交覆盖原记录）
 * 时间字段对应 DATETIME(3)，与存量表的 Long 毫秒约定区分
 */
@Data
@TableName("t_certification")
public class Certification {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 申请用户 ID */
    private String userId;

    /** 真实姓名 */
    private String realName;

    /** 性别 MALE-男 FEMALE-女 */
    private String gender;

    /** 学号 */
    private String studentNo;

    /** 学生证照片 URL */
    private String studentCardImage;

    /** 状态 PENDING-待审核 APPROVED-已通过 REJECTED-已驳回 */
    private String status;

    /** 驳回原因（审核不通过时必填） */
    private String rejectReason;

    /** 审核人（管理员）ID */
    private String reviewerId;

    /** 审核时间 */
    private LocalDateTime reviewTime;

    @TableLogic
    private Integer deleted;

    /** 申请时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
