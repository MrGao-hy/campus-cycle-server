package com.campus.cycle.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Map;

/**
 * 会话
 */
@Data
@TableName(value = "t_conversation", autoResultMap = true)
public class Conversation {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 商品 ID */
    private String goodsId;

    /** 买家 ID */
    private String buyerId;

    /** 卖家 ID */
    private String sellerId;

    /** 最后一条消息 */
    private String lastMessage;

    /** 最后消息时间(ms) */
    private Long lastTime;

    /** 各自维度未读数 {"userId": count} */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Integer> unreadFor;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;
}
