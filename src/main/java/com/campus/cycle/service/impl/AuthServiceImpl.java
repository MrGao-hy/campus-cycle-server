package com.campus.cycle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.cycle.common.exception.BusinessException;
import com.campus.cycle.common.util.Assemblers;
import com.campus.cycle.dto.LoginDTO;
import com.campus.cycle.entity.User;
import com.campus.cycle.mapper.UserMapper;
import com.campus.cycle.security.JwtUtil;
import com.campus.cycle.service.AuthService;
import com.campus.cycle.service.SchoolService;
import com.campus.cycle.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 认证服务
 * mock 模式：按 jsCode 直接签发测试 token（本地联调无需真实微信）
 * 真实模式：jsCode → code2session 换 openid → 签发 token
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_SCHOOL_ID = "s_0001";

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final SchoolService schoolService;

    @Value("${campus.wx.appid}")
    private String appid;

    @Value("${campus.wx.secret}")
    private String secret;

    @Value("${campus.wx.mock:true}")
    private boolean mock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO dto) {
        String openid = mock ? mockOpenid(dto.getJsCode()) : wxCode2Session(dto.getJsCode());

        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getOpenid, openid));
        if (user == null) {
            user = createDefaultUser(openid);
        }
        String token = jwtUtil.createToken(user.getId());
        return new LoginVO(token, Assemblers.toProfile(user, schoolService.nameOf(user.getSchoolId())));
    }

    /** mock 模式 openid：mock_ + jsCode */
    private String mockOpenid(String jsCode) {
        return "mock_" + jsCode;
    }

    /** 真实模式：调微信 code2session 接口 */
    private String wxCode2Session(String jsCode) {
        if (!appid.isBlank() && !secret.isBlank()) {
            RestClient restClient = RestClient.builder().build();
            Map<?, ?> resp = restClient.get()
                    .uri("https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={jsCode}&grant_type=authorization_code",
                            appid, secret, jsCode)
                    .retrieve()
                    .body(Map.class);
            if (resp != null && resp.get("errcode") != null && ((Number) resp.get("errcode")).intValue() != 0) {
                log.error("微信 code2session 失败: {}", resp);
                throw new BusinessException("微信登录失败，请重试");
            }
            Object openid = resp != null ? resp.get("openid") : null;
            if (openid == null) {
                throw new BusinessException("微信登录失败，未获取到 openid");
            }
            return openid.toString();
        }
        // 未配置 appid/secret 时退回 mock
        log.warn("微信 appid/secret 未配置，自动降级为 mock 登录");
        return mockOpenid(jsCode);
    }

    /** 首次登录创建默认用户 */
    private User createDefaultUser(String openid) {
        User user = new User();
        user.setOpenid(openid);
        user.setNickname("同学" + (1000 + ThreadLocalRandom.current().nextInt(9000)));
        user.setSchoolId(DEFAULT_SCHOOL_ID);
        user.setCreditScore(100);
        user.setSuccessCount(0);
        user.setStatus(1);
        userMapper.insert(user);
        return user;
    }
}
