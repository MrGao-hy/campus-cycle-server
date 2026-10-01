package com.campus.cycle.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品
 */
@Data
@TableName(value = "t_goods", autoResultMap = true)
public class Goods {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 卖家 ID */
    private String sellerId;

    /** 所属学校 */
    private String schoolId;

    /** 标题 */
    private String title;

    /** 售价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 图片 URL 数组 */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    /** 分类 */
    private String category;

    /** 成色（condition 为 MySQL 保留字，需反引号转义） */
    @TableField("`condition`")
    private String condition;

    /** 描述 */
    private String description;

    /** 状态 ON_SALE / LOCKED / SOLD */
    private String status;

    /** 浏览量 */
    private Integer views;

    /** 想要/申请数 */
    private Integer wantCount;

    /** 发布时间(ms) */
    private Long publishTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;
}
