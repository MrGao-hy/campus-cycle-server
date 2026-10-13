package com.campus.cycle.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.cycle.common.constant.CertStatus;
import com.campus.cycle.common.constant.UserRole;
import com.campus.cycle.common.exception.BusinessException;
import com.campus.cycle.common.result.ResultCode;
import com.campus.cycle.common.util.Assemblers;
import com.campus.cycle.dto.ApplyCertificationDTO;
import com.campus.cycle.dto.ReviewCertificationDTO;
import com.campus.cycle.entity.Certification;
import com.campus.cycle.entity.User;
import com.campus.cycle.mapper.CertificationMapper;
import com.campus.cycle.mapper.UserMapper;
import com.campus.cycle.security.UserContext;
import com.campus.cycle.service.CertificationService;
import com.campus.cycle.vo.CertificationVO;
import com.campus.cycle.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 校园认证服务
 */
@Service
@RequiredArgsConstructor
public class CertificationServiceImpl implements CertificationService {

    /** 单页条数上限，防止前端传超大 pageSize 拖垮查询 */
    private static final long MAX_PAGE_SIZE = 100;

    /** 默认每页条数 */
    private static final long DEFAULT_PAGE_SIZE = 20;

    private final CertificationMapper certificationMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CertificationVO apply(ApplyCertificationDTO dto) {
        String uid = UserContext.requireUserId();
        Certification exists = certificationMapper.selectOne(Wrappers.<Certification>lambdaQuery()
                .eq(Certification::getUserId, uid));
        if (exists != null) {
            if (CertStatus.APPROVED.equals(exists.getStatus())) {
                throw new BusinessException("您已通过校园认证，无需重复提交");
            }
            if (CertStatus.PENDING.equals(exists.getStatus())) {
                throw new BusinessException("认证审核中，请耐心等待结果");
            }
        }

        String realName = dto.getRealName().trim();
        String studentNo = dto.getStudentNo().trim();
        String image = dto.getStudentCardImage().trim();
        if (exists == null) {
            Certification cert = new Certification();
            cert.setUserId(uid);
            cert.setRealName(realName);
            cert.setGender(dto.getGender());
            cert.setStudentNo(studentNo);
            cert.setStudentCardImage(image);
            cert.setStatus(CertStatus.PENDING);
            certificationMapper.insert(cert);
            return Assemblers.toCertVO(cert, null);
        }
        // 驳回后重新提交：覆盖原记录并清空上次审核痕迹
        //（updateById 默认不写 null 字段，故用 lambdaUpdate 显式置空）
        certificationMapper.update(null, Wrappers.<Certification>lambdaUpdate()
                .eq(Certification::getId, exists.getId())
                .set(Certification::getRealName, realName)
                .set(Certification::getGender, dto.getGender())
                .set(Certification::getStudentNo, studentNo)
                .set(Certification::getStudentCardImage, image)
                .set(Certification::getStatus, CertStatus.PENDING)
                .set(Certification::getRejectReason, null)
                .set(Certification::getReviewerId, null)
                .set(Certification::getReviewTime, null)
                .set(Certification::getUpdateTime, LocalDateTime.now()));
        exists.setRealName(realName);
        exists.setGender(dto.getGender());
        exists.setStudentNo(studentNo);
        exists.setStudentCardImage(image);
        exists.setStatus(CertStatus.PENDING);
        exists.setRejectReason(null);
        exists.setReviewerId(null);
        exists.setReviewTime(null);
        return Assemblers.toCertVO(exists, null);
    }

    @Override
    public CertificationVO myStatus() {
        String uid = UserContext.requireUserId();
        Certification cert = certificationMapper.selectOne(Wrappers.<Certification>lambdaQuery()
                .eq(Certification::getUserId, uid));
        if (cert == null) {
            CertificationVO vo = new CertificationVO();
            vo.setUserId(uid);
            vo.setStatus(CertStatus.NONE);
            return vo;
        }
        return Assemblers.toCertVO(cert, null);
    }

    @Override
    public PageResult<CertificationVO> adminList(String status, long pageNum, long pageSize) {
        requireAdmin();
        long size = pageSize > 0 ? Math.min(pageSize, MAX_PAGE_SIZE) : DEFAULT_PAGE_SIZE;
        long current = pageNum > 0 ? pageNum : 1;
        var query = Wrappers.<Certification>lambdaQuery();
        if (StringUtils.hasText(status)) {
            query.eq(Certification::getStatus, status.trim());
        }
        // 待审核优先，已通过/已驳回靠后，同类按申请时间倒序
        query.last("ORDER BY CASE status WHEN 'PENDING' THEN 0 WHEN 'APPROVED' THEN 1 ELSE 2 END, create_time DESC");

        Page<Certification> page = new Page<>(current, size);
        IPage<Certification> result = certificationMapper.selectPage(page, query);
        // 批量填充申请人昵称，方便后台列表展示
        List<String> userIds = result.getRecords().stream().map(Certification::getUserId).distinct().toList();
        Map<String, String> nicknameMap = userIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));
        List<CertificationVO> vos = result.getRecords().stream()
                .map(c -> Assemblers.toCertVO(c, nicknameMap.get(c.getUserId())))
                .toList();
        return PageResult.of(vos, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CertificationVO review(ReviewCertificationDTO dto) {
        String adminId = requireAdmin();
        Certification cert = certificationMapper.selectById(dto.getId());
        if (cert == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "认证申请不存在");
        }
        if (!CertStatus.PENDING.equals(cert.getStatus())) {
            throw new BusinessException("该申请已处理，请勿重复审核");
        }
        User applicant = userMapper.selectById(cert.getUserId());
        cert.setReviewerId(adminId);
        cert.setReviewTime(LocalDateTime.now());
        if (Boolean.TRUE.equals(dto.getApprove())) {
            cert.setStatus(CertStatus.APPROVED);
            // 通过后冗余更新用户认证标记（资料页徽章展示）
            if (applicant != null) {
                applicant.setCertified(1);
                userMapper.updateById(applicant);
            }
        } else {
            if (!StringUtils.hasText(dto.getReason())) {
                throw new BusinessException("不通过时必须填写驳回原因");
            }
            cert.setStatus(CertStatus.REJECTED);
            cert.setRejectReason(dto.getReason().trim());
        }
        certificationMapper.updateById(cert);
        return Assemblers.toCertVO(cert, applicant == null ? null : applicant.getNickname());
    }

    /** 校验当前登录用户为管理员，返回其 ID */
    private String requireAdmin() {
        String uid = UserContext.requireUserId();
        User admin = userMapper.selectById(uid);
        if (admin == null || !UserRole.ADMIN.equals(admin.getRole())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限操作，仅管理员可访问");
        }
        return uid;
    }
}
