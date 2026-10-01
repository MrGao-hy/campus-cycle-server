package com.campus.cycle.common.util;

import com.campus.cycle.entity.Review;
import com.campus.cycle.entity.User;
import com.campus.cycle.vo.ContactVO;
import com.campus.cycle.vo.ReviewVO;
import com.campus.cycle.vo.UserProfileVO;

/**
 * 实体 → VO 装配器（对齐前端契约结构）
 */
public final class Assemblers {

    private Assemblers() {
    }

    /** User → UserProfileVO（contact 组装为嵌套结构） */
    public static UserProfileVO toProfile(User u) {
        return toProfile(u, null);
    }

    /** User → UserProfileVO（带学校名称，schoolName 可为 null） */
    public static UserProfileVO toProfile(User u, String schoolName) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(u.getId());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setSchoolId(u.getSchoolId());
        vo.setSchoolName(schoolName);
        vo.setCreditScore(u.getCreditScore());
        vo.setSuccessCount(u.getSuccessCount());
        ContactVO contact = new ContactVO();
        contact.setPhone(u.getContactPhone());
        contact.setQq(u.getContactQq());
        contact.setWechat(u.getContactWechat());
        contact.setEmail(u.getContactEmail());
        vo.setContact(contact);
        return vo;
    }

    /** Review → ReviewVO */
    public static ReviewVO toReviewVO(Review r) {
        ReviewVO vo = new ReviewVO();
        vo.setId(r.getId());
        vo.setGoodsId(r.getGoodsId());
        vo.setOrderId(r.getOrderId());
        vo.setFromUserId(r.getFromUserId());
        vo.setFromNickname(r.getFromNickname());
        vo.setRate(r.getRate());
        vo.setContent(r.getContent());
        vo.setTime(r.getTime());
        return vo;
    }
}
