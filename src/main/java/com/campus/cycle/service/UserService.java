package com.campus.cycle.service;

import com.campus.cycle.dto.UpdateProfileDTO;
import com.campus.cycle.vo.UserDetailVO;
import com.campus.cycle.vo.UserProfileVO;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 更新个人资料（微信授权头像昵称）
     */
    UserProfileVO updateProfile(UpdateProfileDTO dto);

    /**
     * 用户主页（资料 + 在售商品 + 收到的评价）
     *
     * @param userId 目标用户 id
     */
    UserDetailVO detail(String userId);
}
