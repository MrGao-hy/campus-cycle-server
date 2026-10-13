package com.campus.cycle.controller;

import com.campus.cycle.common.result.Result;
import com.campus.cycle.dto.ApplyCertificationDTO;
import com.campus.cycle.dto.ReviewCertificationDTO;
import com.campus.cycle.service.CertificationService;
import com.campus.cycle.service.UploadService;
import com.campus.cycle.vo.CertificationVO;
import com.campus.cycle.vo.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 校园认证接口（用户端申请 + 后台审核）
 */
@Tag(name = "校园认证")
@RestController
@RequestMapping("/certification")
@RequiredArgsConstructor
public class CertificationController {

    private final CertificationService certificationService;
    private final UploadService uploadService;

    @Operation(summary = "上传学生证照片（multipart，返回可访问 URL）")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        return Result.success(uploadService.saveImage(file, request));
    }

    @Operation(summary = "提交校园认证申请（姓名/性别/学号/学生证照片；驳回后可重新提交）")
    @PostMapping("/apply")
    public Result<CertificationVO> apply(@Valid @RequestBody ApplyCertificationDTO dto) {
        return Result.success(certificationService.apply(dto));
    }

    @Operation(summary = "我的认证状态（未申请 status=NONE；已驳回返回 rejectReason）")
    @GetMapping("/status")
    public Result<CertificationVO> myStatus() {
        return Result.success(certificationService.myStatus());
    }

    @Operation(summary = "后台：认证申请列表（管理员；status 筛选 PENDING/APPROVED/REJECTED + 分页）")
    @GetMapping("/admin/list")
    public Result<PageResult<CertificationVO>> adminList(@RequestParam(required = false) String status,
                                                         @RequestParam(required = false, defaultValue = "1") long pageNum,
                                                         @RequestParam(required = false, defaultValue = "0") long pageSize) {
        return Result.success(certificationService.adminList(status, pageNum, pageSize));
    }

    @Operation(summary = "后台：审核认证申请（管理员；approve=true 通过 / false 不通过需填 reason）")
    @PostMapping("/admin/review")
    public Result<CertificationVO> review(@Valid @RequestBody ReviewCertificationDTO dto) {
        return Result.success(certificationService.review(dto));
    }
}
