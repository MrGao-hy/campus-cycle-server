package com.campus.cycle.controller;

import com.campus.cycle.common.result.Result;
import com.campus.cycle.dto.UpdateProfileDTO;
import com.campus.cycle.service.UserService;
import com.campus.cycle.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口
 */
@Tag(name = "用户")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "更新个人资料（微信授权头像昵称）")
    @PostMapping("/profile")
    public Result<UserProfileVO> updateProfile(@Valid @RequestBody UpdateProfileDTO dto) {
        return Result.success(userService.updateProfile(dto));
    }
}
