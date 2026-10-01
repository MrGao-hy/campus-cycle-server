package com.campus.cycle.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 聊天消息
 */
@Data
@TableName("t_message")
public class Message {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 会话 ID */
    private String conversationId;

    /** 发送人 ID */
    private String fromUserId;

    /** 消息内容 */
    private String content;

    /** 发送时间(ms) */
    private Long time;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;
}
