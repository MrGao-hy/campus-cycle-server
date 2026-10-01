package com.campus.cycle.service.impl;

import com.campus.cycle.common.exception.BusinessException;
import com.campus.cycle.common.result.ResultCode;
import com.campus.cycle.common.util.Assemblers;
import com.campus.cycle.dto.UpdateProfileDTO;
import com.campus.cycle.entity.User;
import com.campus.cycle.mapper.UserMapper;
import com.campus.cycle.security.UserContext;
import com.campus.cycle.service.UserService;
import com.campus.cycle.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateProfile(UpdateProfileDTO dto) {
        String uid = UserContext.requireUserId();
        User user = userMapper.selectById(uid);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (dto.getNickname() != null && !dto.getNickname().isBlank()) {
            user.setNickname(dto.getNickname().trim());
        }
        if (dto.getAvatar() != null && !dto.getAvatar().isBlank()) {
            user.setAvatar(dto.getAvatar().trim());
        }
        userMapper.updateById(user);
        return Assemblers.toProfile(user);
    }
}
