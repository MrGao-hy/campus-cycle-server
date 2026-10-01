package com.campus.cycle.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 学校
 */
@Data
@TableName("t_school")
public class School {

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /** 学校全称 */
    private String name;

    /** 学校简称 */
    private String shortName;

    /** 在售商品数（冗余统计） */
    private Integer goodsCount;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private Long createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateTime;
}
