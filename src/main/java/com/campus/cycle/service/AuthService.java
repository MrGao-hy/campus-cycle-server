package com.campus.cycle.service;

import com.campus.cycle.dto.LoginDTO;
import com.campus.cycle.vo.LoginVO;

/**
 * 认证服务
 */
public interface AuthService {

    /**
     * 微信登录：mock 模式直接签发，真实模式 code2session 换 openid
     */
    LoginVO login(LoginDTO dto);
}
