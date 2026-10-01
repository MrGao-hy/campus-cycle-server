package com.campus.cycle.service;

import com.campus.cycle.entity.School;

import java.util.List;

/**
 * 学校服务
 */
public interface SchoolService {

    /** 学校列表（支持按名称/简称模糊搜索） */
    List<School> list(String keyword);

    /** 确认学校（更新当前用户所属学校） */
    School confirm(String schoolId);
}
