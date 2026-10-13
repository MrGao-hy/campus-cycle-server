package com.campus.cycle.service;

import com.campus.cycle.dto.ApplyCertificationDTO;
import com.campus.cycle.dto.ReviewCertificationDTO;
import com.campus.cycle.vo.CertificationVO;
import com.campus.cycle.vo.PageResult;

/**
 * 校园认证服务
 */
public interface CertificationService {

    /** 提交认证申请（首次申请或驳回后重新提交） */
    CertificationVO apply(ApplyCertificationDTO dto);

    /** 我的认证状态（未申请时 status=NONE） */
    CertificationVO myStatus();

    /** 后台：认证申请列表（管理员；status 筛选 + 分页） */
    PageResult<CertificationVO> adminList(String status, long pageNum, long pageSize);

    /** 后台：审核通过/不通过（管理员；不通过时 reason 必填） */
    CertificationVO review(ReviewCertificationDTO dto);
}
