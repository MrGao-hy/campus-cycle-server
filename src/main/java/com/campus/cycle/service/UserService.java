package com.campus.cycle.service;

import com.campus.cycle.dto.UpdateProfileDTO;
import com.campus.cycle.vo.UserProfileVO;

/**
 * 用户服务
 */
public interface UserService {

    /**
     * 更新个人资料（微信授权头像昵称）
     */
    UserProfileVO updateProfile(UpdateProfileDTO dto);
}
