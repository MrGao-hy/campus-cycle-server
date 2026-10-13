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
 * 用户
 */
@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 微信 openid（mock 模式可为空） */
    private String openid;

    /** 昵称 */
    private String nickname;

    /** 头像 URL */
    private String avatar;

    /** 所属学校 */
    private String schoolId;

    /** 信用分 */
    private Integer creditScore;

    /** 成功交易笔数（首单免手续费依据） */
    private Integer successCount;

    /** 联系方式-电话 */
    private String contactPhone;

    /** 联系方式-QQ */
    private String contactQq;

    /** 联系方式-微信 */
    private String contactWechat;

    /** 联系方式-邮箱 */
    private String contactEmail;

    /** 角色 USER-普通用户 ADMIN-管理员（后台审核） */
    private String role;

    /** 校园认证 0-未认证 1-已认证（审核通过时冗余更新） */
    private Integer certified;

    /** 状态 1-正常 0-禁用 */
    private Integer status;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
