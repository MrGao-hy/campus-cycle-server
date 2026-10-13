package com.campus.cycle.common.util;

import com.campus.cycle.entity.Certification;
import com.campus.cycle.entity.Review;
import com.campus.cycle.entity.User;
import com.campus.cycle.vo.CertificationVO;
import com.campus.cycle.vo.ContactVO;
import com.campus.cycle.vo.ReviewVO;
import com.campus.cycle.vo.UserProfileVO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 实体 → VO 装配器（对齐前端契约结构）
 */
public final class Assemblers {

    private Assemblers() {
    }

    /** LocalDateTime → Date（VO 时间字段用 Date，Jackson 全局 date-format 输出） */
    public static Date toDate(LocalDateTime time) {
        return time == null ? null : Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
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
        vo.setCertified(u.getCertified());
        ContactVO contact = new ContactVO();
        contact.setContactPhone(u.getContactPhone());
        contact.setContactQq(u.getContactQq());
        contact.setContactWechat(u.getContactWechat());
        contact.setContactEmail(u.getContactEmail());
        vo.setContact(contact);
        return vo;
    }

    /** Certification → CertificationVO（nickname 可为 null，用户端查询不填） */
    public static CertificationVO toCertVO(Certification c, String nickname) {
        CertificationVO vo = new CertificationVO();
        vo.setId(c.getId());
        vo.setUserId(c.getUserId());
        vo.setNickname(nickname);
        vo.setRealName(c.getRealName());
        vo.setGender(c.getGender());
        vo.setStudentNo(c.getStudentNo());
        vo.setStudentCardImage(c.getStudentCardImage());
        vo.setStatus(c.getStatus());
        vo.setRejectReason(c.getRejectReason());
        vo.setApplyTime(toDate(c.getCreateTime()));
        vo.setReviewTime(toDate(c.getReviewTime()));
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
