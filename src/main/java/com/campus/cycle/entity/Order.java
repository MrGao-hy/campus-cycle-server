package com.campus.cycle.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单
 * 状态机：PENDING_SELLER → PENDING_OFFLINE → PENDING_BUYER → APPEALING → COMPLETED / CANCELLED
 */
@Data
@TableName("t_order")
public class Order {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 商品 ID */
    private String goodsId;

    /** 买家 ID */
    private String buyerId;

    /** 卖家 ID */
    private String sellerId;

    /** 订单状态 */
    private String status;

    /** 成交价（申请时锁定） */
    private BigDecimal price;

    /** 买家申请时间(ms) */
    private Long applyTime;

    /** 卖家处理截止(ms)，申请后 24h 自动过期不收手续费 */
    private Long expireTime;

    /** 卖家确认时间(ms) */
    private Long sellerConfirmTime;

    /** 买家确认时间(ms) */
    private Long buyerConfirmTime;

    /** 申诉期截止(ms) = 买家确认 + 48h */
    private Long appealEndTime;

    /** 申诉期内卖家是否提出异议 */
    private Boolean sellerObjection;

    /** 取消时间(ms) */
    private Long cancelTime;

    /** 取消原因 */
    private String cancelReason;

    /** 完成时间(ms) */
    private Long completeTime;

    /** 手续费是否已生成 */
    private Boolean feeBilled;

    /** 买家申请留言 */
    private String remark;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;
}
