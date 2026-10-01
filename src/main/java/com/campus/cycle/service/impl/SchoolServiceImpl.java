package com.campus.cycle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.cycle.common.exception.BusinessException;
import com.campus.cycle.common.result.ResultCode;
import com.campus.cycle.entity.School;
import com.campus.cycle.entity.User;
import com.campus.cycle.mapper.SchoolMapper;
import com.campus.cycle.mapper.UserMapper;
import com.campus.cycle.security.UserContext;
import com.campus.cycle.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 学校服务
 */
@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolMapper schoolMapper;
    private final UserMapper userMapper;

    @Override
    public List<School> list(String keyword) {
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            return schoolMapper.selectList(Wrappers.<School>lambdaQuery()
                    .like(School::getName, kw)
                    .or()
                    .like(School::getShortName, kw)
                    .orderByAsc(School::getId));
        }
        return schoolMapper.selectList(Wrappers.<School>lambdaQuery().orderByAsc(School::getId));
    }

    @Override
    public School confirm(String schoolId) {
        School school = schoolMapper.selectById(schoolId);
        if (school == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "学校不存在");
        }
        User user = userMapper.selectById(UserContext.requireUserId());
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setSchoolId(schoolId);
        userMapper.updateById(user);
        return school;
    }

    @Override
    public String nameOf(String schoolId) {
        if (!StringUtils.hasText(schoolId)) {
            return null;
        }
        School school = schoolMapper.selectById(schoolId);
        return school == null ? null : school.getName();
    }
}
