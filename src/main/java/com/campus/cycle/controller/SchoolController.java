package com.campus.cycle.controller;

import com.campus.cycle.common.result.Result;
import com.campus.cycle.dto.ConfirmSchoolDTO;
import com.campus.cycle.entity.School;
import com.campus.cycle.service.SchoolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学校接口
 */
@Tag(name = "学校")
@RestController
@RequestMapping("/school")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    @Operation(summary = "学校列表（支持模糊搜索）")
    @GetMapping("/list")
    public Result<List<School>> list(@RequestParam(required = false) String keyword) {
        return Result.success(schoolService.list(keyword));
    }

    @Operation(summary = "确认学校（选择后仅浏览本校商品）")
    @PostMapping("/confirm")
    public Result<School> confirm(@Valid @RequestBody ConfirmSchoolDTO dto) {
        return Result.success(schoolService.confirm(dto.getSchoolId()));
    }
}
