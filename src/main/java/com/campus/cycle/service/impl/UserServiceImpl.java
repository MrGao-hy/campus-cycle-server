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
        userMapper.updateById(user);
        return Assemblers.toProfile(user, schoolService.nameOf(user.getSchoolId()));
    }

    @Override
    public UserDetailVO detail(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        // 在售/锁定商品（供对方继续浏览，已售出不展示）
        List<Goods> onSaleGoods = goodsMapper.selectList(Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getSellerId, userId)
                .in(Goods::getStatus, GoodsStatus.ON_SALE, GoodsStatus.LOCKED)
                .orderByDesc(Goods::getPublishTime));
        // 收到的评价 = 该用户作为卖家发布的商品收到的评价
        List<Review> reviews = reviewMapper.selectList(Wrappers.<Review>lambdaQuery()
                .in(Review::getGoodsId, onSaleGoods.stream().map(Goods::getId).toList())
                .orderByDesc(Review::getTime));

        UserDetailVO vo = new UserDetailVO();
        vo.setProfile(Assemblers.toProfile(user, schoolService.nameOf(user.getSchoolId())));
        vo.setOnSaleGoods(onSaleGoods);
        vo.setReviews(reviews.stream().map(Assemblers::toReviewVO).toList());
        return vo;
    }
}
