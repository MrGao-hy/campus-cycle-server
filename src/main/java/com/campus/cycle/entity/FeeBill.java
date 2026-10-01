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
 * 手续费账单
 */
@Data
@TableName("t_fee_bill")
public class FeeBill {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 订单 ID（一单一账） */
    private String orderId;

    /** 卖家 ID */
    private String sellerId;

    /** 商品标题快照 */
    private String goodsTitle;

    /** 成交价 */
    private BigDecimal dealPrice;

    /** 费率 0.06 */
    private BigDecimal rate;

    /** 应收金额（首单为 0） */
    private BigDecimal amount;

    /** 免手续费原因（如：首单免费） */
    private String freeReason;

    /** 状态 UNPAID / PAID */
    private String status;

    /** 账单生成时间(ms) */
    private Long createTime;

    /** 支付时间(ms) */
    private Long payTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;
}
