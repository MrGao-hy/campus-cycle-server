package com.campus.cycle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.cycle.common.constant.GoodsStatus;
import com.campus.cycle.common.exception.BusinessException;
import com.campus.cycle.common.result.ResultCode;
import com.campus.cycle.common.util.Assemblers;
import com.campus.cycle.dto.UpdateProfileDTO;
import com.campus.cycle.entity.Goods;
import com.campus.cycle.entity.Review;
import com.campus.cycle.entity.User;
import com.campus.cycle.mapper.GoodsMapper;
import com.campus.cycle.mapper.ReviewMapper;
import com.campus.cycle.mapper.UserMapper;
import com.campus.cycle.security.UserContext;
import com.campus.cycle.service.SchoolService;
import com.campus.cycle.service.UserService;
import com.campus.cycle.vo.UserDetailVO;
import com.campus.cycle.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final ReviewMapper reviewMapper;
    private final SchoolService schoolService;

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
        if (dto.getContactPhone() != null) {
            user.setContactPhone(dto.getContactPhone().trim());
        }
        if (dto.getContactQq() != null) {
            user.setContactQq(dto.getContactQq().trim());
        }
        if (dto.getContactWechat() != null) {
            user.setContactWechat(dto.getContactWechat().trim());
        }
        if (dto.getContactEmail() != null) {
            user.setContactEmail(dto.getContactEmail().trim());
        }
        userMapper.updateById(user);
        return Assemblers.toProfile(user, schoolService.nameOf(user.getSchoolId()));
    }

    @Override
    public UserDetailVO detail(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        // 在售/锁定/已售出商品（已售置灰展示不隐藏，排序与商品列表一致：在售优先）
        List<Goods> onSaleGoods = goodsMapper.selectList(Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getSellerId, userId)
                .in(Goods::getStatus, GoodsStatus.ON_SALE, GoodsStatus.LOCKED, GoodsStatus.SOLD)
                .last("ORDER BY CASE status WHEN 'ON_SALE' THEN 0 WHEN 'LOCKED' THEN 1 ELSE 2 END, publish_time DESC"));
        // 收到的评价 = 该卖家全部商品（含已售出）收到的评价。
        // 只有 COMPLETED 订单才能评价，而订单完成时商品已置 SOLD，
        // 评价基本都挂在已售出商品上，不能只查在售商品（否则评价永远为空）
        List<String> goodsIds = goodsMapper.selectList(Wrappers.<Goods>lambdaQuery()
                        .select(Goods::getId)
                        .eq(Goods::getSellerId, userId))
                .stream().map(Goods::getId).toList();
        List<Review> reviews = goodsIds.isEmpty()
                ? List.of()
                : reviewMapper.selectList(Wrappers.<Review>lambdaQuery()
                        .in(Review::getGoodsId, goodsIds)
                        .orderByDesc(Review::getTime));

        UserDetailVO vo = new UserDetailVO();
        vo.setProfile(Assemblers.toProfile(user, schoolService.nameOf(user.getSchoolId())));
        vo.setOnSaleGoods(onSaleGoods);
        vo.setReviews(reviews.stream().map(Assemblers::toReviewVO).toList());
        return vo;
    }
}
