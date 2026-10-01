package com.campus.cycle.vo;

import lombok.Data;

/**
 * 用户资料（对齐前端 UserProfile 结构）
 */
@Data
public class UserProfileVO {

    private String id;
    private String nickname;
    private String avatar;
    private String schoolId;

    /** 信用分 */
    private Integer creditScore;

    /** 成功交易笔数（首单免手续费判断依据） */
    private Integer successCount;

    /** 站内联系方式 */
    private ContactVO contact;
}
