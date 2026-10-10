package com.campus.cycle.vo;

import lombok.Data;

/**
 * 站内联系方式（卖家确认后展示）
 */
@Data
public class ContactVO {

    private String contactPhone;
    private String contactQq;
    private String contactWechat;
    private String contactEmail;
}
