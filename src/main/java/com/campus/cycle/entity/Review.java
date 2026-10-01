package com.campus.cycle.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品评价
 */
@Data
@TableName("t_review")
public class Review {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 商品 ID */
    private String goodsId;

    /** 订单 ID */
    private String orderId;

    /** 评价人 ID */
    private String fromUserId;

    /** 评价人昵称（冗余快照） */
    private String fromNickname;

    /** 评分 1-5 */
    private Integer rate;

    /** 评价内容 */
    private String content;

    /** 评价时间(ms) */
    private Long time;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;
}
