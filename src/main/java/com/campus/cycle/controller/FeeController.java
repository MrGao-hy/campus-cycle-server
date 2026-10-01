package com.campus.cycle.controller;

import com.campus.cycle.common.result.Result;
import com.campus.cycle.dto.PayFeeDTO;
import com.campus.cycle.entity.FeeBill;
import com.campus.cycle.service.FeeService;
import com.campus.cycle.vo.CheckPublishVO;
import com.campus.cycle.vo.FeeSummaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 手续费账单接口
 */
@Tag(name = "手续费账单")
@RestController
@RequestMapping("/fee")
@RequiredArgsConstructor
public class FeeController {

    private final FeeService feeService;

    @Operation(summary = "手续费账单汇总（未结清禁止发布新商品）")
    @GetMapping("/summary")
    public Result<FeeSummaryVO> summary() {
        return Result.success(feeService.summary());
    }

    @Operation(summary = "账单列表")
    @GetMapping("/bills")
    public Result<List<FeeBill>> bills() {
        return Result.success(feeService.bills());
    }

    @Operation(summary = "支付手续费")
    @PostMapping("/pay")
    public Result<Void> pay(@Valid @RequestBody PayFeeDTO dto) {
        feeService.pay(dto.getBillId());
        return Result.success();
    }

    @Operation(summary = "是否允许发布新商品")
    @GetMapping("/check-publish")
    public Result<CheckPublishVO> checkPublish() {
        return Result.success(feeService.checkPublish());
    }
}
