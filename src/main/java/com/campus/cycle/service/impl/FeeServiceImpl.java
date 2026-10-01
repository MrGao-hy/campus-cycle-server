package com.campus.cycle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.cycle.common.constant.FeeRules;
import com.campus.cycle.common.exception.BusinessException;
import com.campus.cycle.common.result.ResultCode;
import com.campus.cycle.entity.FeeBill;
import com.campus.cycle.mapper.FeeBillMapper;
import com.campus.cycle.security.UserContext;
import com.campus.cycle.service.FeeService;
import com.campus.cycle.vo.CheckPublishVO;
import com.campus.cycle.vo.FeeSummaryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 手续费服务
 */
@Service
@RequiredArgsConstructor
public class FeeServiceImpl implements FeeService {

    private final FeeBillMapper feeBillMapper;

    @Override
    public FeeSummaryVO summary() {
        String sellerId = UserContext.requireUserId();
        List<FeeBill> bills = feeBillMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .eq(FeeBill::getSellerId, sellerId)
                .orderByDesc(FeeBill::getCreateTime));
        BigDecimal unpaidAmount = bills.stream()
                .filter(b -> FeeRules.STATUS_UNPAID.equals(b.getStatus()))
                .map(FeeBill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long unpaidCount = bills.stream()
                .filter(b -> FeeRules.STATUS_UNPAID.equals(b.getStatus()))
                .count();
        FeeSummaryVO vo = new FeeSummaryVO();
        vo.setUnpaidAmount(unpaidAmount);
        vo.setUnpaidCount((int) unpaidCount);
        vo.setBills(bills);
        return vo;
    }

    @Override
    public List<FeeBill> bills() {
        String sellerId = UserContext.requireUserId();
        return feeBillMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .eq(FeeBill::getSellerId, sellerId)
                .orderByDesc(FeeBill::getCreateTime));
    }

    @Override
    public void pay(String billId) {
        String sellerId = UserContext.requireUserId();
        FeeBill bill = feeBillMapper.selectById(billId);
        if (bill == null || !sellerId.equals(bill.getSellerId())) {
            throw new BusinessException(ResultCode.NOT_FOUND, "账单不存在");
        }
        if (FeeRules.STATUS_PAID.equals(bill.getStatus())) {
            throw new BusinessException("账单已支付");
        }
        bill.setStatus(FeeRules.STATUS_PAID);
        bill.setPayTime(System.currentTimeMillis());
        feeBillMapper.updateById(bill);
    }

    @Override
    public CheckPublishVO checkPublish() {
        String sellerId = UserContext.requireUserId();
        List<FeeBill> unpaid = feeBillMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .eq(FeeBill::getSellerId, sellerId)
                .eq(FeeBill::getStatus, FeeRules.STATUS_UNPAID));
        BigDecimal unpaidAmount = unpaid.stream()
                .map(FeeBill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CheckPublishVO(unpaidAmount.compareTo(BigDecimal.ZERO) <= 0, unpaidAmount);
    }
}
