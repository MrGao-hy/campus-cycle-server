package com.campus.cycle.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.cycle.common.constant.GoodsStatus;
import com.campus.cycle.common.constant.OrderStatus;
import com.campus.cycle.common.exception.BusinessException;
import com.campus.cycle.common.result.ResultCode;
import com.campus.cycle.common.util.Assemblers;
import com.campus.cycle.dto.ApplyBuyDTO;
import com.campus.cycle.dto.PublishGoodsDTO;
import com.campus.cycle.entity.FeeBill;
import com.campus.cycle.entity.Goods;
import com.campus.cycle.entity.Conversation;
import com.campus.cycle.entity.Order;
import com.campus.cycle.entity.Review;
import com.campus.cycle.entity.User;
import com.campus.cycle.mapper.ConversationMapper;
import com.campus.cycle.mapper.FeeBillMapper;
import com.campus.cycle.mapper.GoodsMapper;
import com.campus.cycle.mapper.OrderMapper;
import com.campus.cycle.mapper.ReviewMapper;
import com.campus.cycle.mapper.UserMapper;
import com.campus.cycle.security.UserContext;
import com.campus.cycle.service.GoodsService;
import com.campus.cycle.service.SchoolService;
import com.campus.cycle.vo.GoodsDetailVO;
import com.campus.cycle.vo.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品服务
 */
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    /** 单页条数上限，防止前端传超大 pageSize 拖垮查询 */
    private static final long MAX_PAGE_SIZE = 100;

    /** 默认每页条数（campus.goods.page-size 在 Nacos 中维护，热更新生效） */
    @Value("${campus.goods.page-size:20}")
    private long defaultPageSize;

    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final FeeBillMapper feeBillMapper;
    private final ConversationMapper conversationMapper;
    private final SchoolService schoolService;

    @Override
    public PageResult<Goods> list(String keyword, String category, long pageNum, long pageSize) {
        // 商品列表依赖登录用户绑定的学校：schoolId 不传，从登录态取（登录/建号时已绑定）
        String userId = UserContext.requireUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        if (!StringUtils.hasText(user.getSchoolId())) {
            throw new BusinessException("请先选择学校");
        }
        // 分页参数兜底：非法值回落到默认每页条数（campus.goods.page-size，可在 Nacos 调整）
        long size = pageSize > 0 ? Math.min(pageSize, MAX_PAGE_SIZE) : defaultPageSize;
        long current = pageNum > 0 ? pageNum : 1;

        var query = Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getSchoolId, user.getSchoolId())
                // 卖家已下架的商品买家不可见
                .ne(Goods::getStatus, GoodsStatus.OFF_SHELF);
        if (StringUtils.hasText(category) && !"推荐".equals(category)) {
            query.eq(Goods::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            query.and(w -> w.like(Goods::getTitle, kw).or().like(Goods::getDescription, kw));
        }
        // 在售优先（LOCKED 置灰中、SOLD 已售出置灰展示均后置），同类按发布时间倒序
        query.last("ORDER BY CASE status WHEN 'ON_SALE' THEN 0 WHEN 'LOCKED' THEN 1 ELSE 2 END, publish_time DESC");

        Page<Goods> page = new Page<>(current, size);
        IPage<Goods> result = goodsMapper.selectPage(page, query);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public GoodsDetailVO detail(String id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已删除");
        }
        User seller = userMapper.selectById(goods.getSellerId());
        if (seller == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "卖家不存在");
        }
        // 浏览量 +1
        goods.setViews(goods.getViews() + 1);
        goodsMapper.updateById(goods);
        List<Review> reviews = reviewMapper.selectList(Wrappers.<Review>lambdaQuery()
                .eq(Review::getGoodsId, id)
                .orderByDesc(Review::getTime));

        GoodsDetailVO vo = new GoodsDetailVO();
        vo.setId(goods.getId());
        vo.setSellerId(goods.getSellerId());
        vo.setSchoolId(goods.getSchoolId());
        vo.setTitle(goods.getTitle());
        vo.setPrice(goods.getPrice());
        vo.setOriginalPrice(goods.getOriginalPrice());
        vo.setImages(goods.getImages());
        vo.setCategory(goods.getCategory());
        vo.setCondition(goods.getCondition());
        vo.setDescription(goods.getDescription());
        vo.setStatus(goods.getStatus());
        vo.setViews(goods.getViews());
        vo.setWantCount(goods.getWantCount());
        vo.setPublishTime(goods.getPublishTime());
        vo.setSeller(Assemblers.toProfile(seller, schoolService.nameOf(seller.getSchoolId())));
        vo.setReviews(reviews.stream().map(Assemblers::toReviewVO).toList());
        return vo;
    }

    @Override
    public List<Goods> mine() {
        String sellerId = UserContext.requireUserId();
        return goodsMapper.selectList(Wrappers.<Goods>lambdaQuery()
                .eq(Goods::getSellerId, sellerId)
                .orderByDesc(Goods::getPublishTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Goods publish(PublishGoodsDTO dto) {
        String sellerId = UserContext.requireUserId();
        // 有未结清手续费时禁止发布
        List<FeeBill> unpaid = feeBillMapper.selectList(Wrappers.<FeeBill>lambdaQuery()
                .eq(FeeBill::getSellerId, sellerId)
                .eq(FeeBill::getStatus, "UNPAID"));
        BigDecimal unpaidAmount = unpaid.stream()
                .map(FeeBill::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (unpaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("您有未结清手续费 " + unpaidAmount + " 元，结清前暂不能发布新商品");
        }

        User user = userMapper.selectById(sellerId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        Goods goods = new Goods();
        goods.setSellerId(sellerId);
        goods.setSchoolId(user.getSchoolId());
        goods.setTitle(dto.getTitle());
        goods.setPrice(dto.getPrice());
        goods.setImages(dto.getImages());
        goods.setCategory(dto.getCategory());
        goods.setCondition(dto.getCondition());
        goods.setDescription(dto.getDescription());
        goods.setStatus(GoodsStatus.ON_SALE);
        goods.setViews(0);
        goods.setWantCount(0);
        goods.setPublishTime(System.currentTimeMillis());
        goodsMapper.insert(goods);
        return goods;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String applyBuy(ApplyBuyDTO dto) {
        String buyerId = UserContext.requireUserId();
        Goods goods = goodsMapper.selectById(dto.getGoodsId());
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        if (buyerId.equals(goods.getSellerId())) {
            throw new BusinessException("不能购买自己发布的商品");
        }
        if (!GoodsStatus.ON_SALE.equals(goods.getStatus())) {
            throw new BusinessException(GoodsStatus.SOLD.equals(goods.getStatus()) ? "商品已售出" : "该商品已有订单进行中");
        }
        // 商品已有进行中订单（待卖家确认/待线下/待买家确认/申诉期）→ 禁止一物多单
        Long active = orderMapper.selectCount(Wrappers.<Order>lambdaQuery()
                .eq(Order::getGoodsId, goods.getId())
                .in(Order::getStatus,
                        OrderStatus.PENDING_SELLER, OrderStatus.PENDING_OFFLINE,
                        OrderStatus.PENDING_BUYER, OrderStatus.APPEALING));
        if (active > 0) {
            throw new BusinessException("该商品已有买家申请进行中，暂不可申请");
        }
        // 同一买家对同一商品仅允许一个待确认申请
        Long pending = orderMapper.selectCount(Wrappers.<Order>lambdaQuery()
                .eq(Order::getGoodsId, goods.getId())
                .eq(Order::getBuyerId, buyerId)
                .eq(Order::getStatus, OrderStatus.PENDING_SELLER));
        if (pending > 0) {
            throw new BusinessException("您已提交过申请，请等待卖家确认");
        }

        long now = System.currentTimeMillis();
        Order order = new Order();
        order.setGoodsId(goods.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getSellerId());
        order.setStatus(OrderStatus.PENDING_SELLER);
        order.setPrice(goods.getPrice());
        order.setApplyTime(now);
        order.setExpireTime(now + OrderStatus.SELLER_EXPIRE_MS);
        order.setRemark(dto.getRemark());
        orderMapper.insert(order);

        // 想要数 +1
        goods.setWantCount(goods.getWantCount() + 1);
        goodsMapper.updateById(goods);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startConversation(String goodsId) {
        String uid = UserContext.requireUserId();
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }
        if (uid.equals(goods.getSellerId())) {
            throw new BusinessException("不能与自己发布的商品发起会话");
        }
        // 复用已存在会话（买卖双方 + 商品维度）
        Conversation exists = conversationMapper.selectOne(Wrappers.<Conversation>lambdaQuery()
                .eq(Conversation::getGoodsId, goodsId)
                .eq(Conversation::getBuyerId, uid));
        if (exists != null) {
            return exists.getId();
        }
        Conversation conversation = new Conversation();
        conversation.setGoodsId(goodsId);
        conversation.setBuyerId(uid);
        conversation.setSellerId(goods.getSellerId());
        conversation.setLastMessage("");
        conversation.setLastTime(System.currentTimeMillis());
        conversation.setUnreadFor(new HashMap<>());
        conversationMapper.insert(conversation);
        return conversation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offShelf(String goodsId) {
        Goods goods = requireMine(goodsId);
        if (!GoodsStatus.ON_SALE.equals(goods.getStatus())) {
            throw new BusinessException("仅「在售」商品可下架");
        }
        goods.setStatus(GoodsStatus.OFF_SHELF);
        goodsMapper.updateById(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onShelf(String goodsId) {
        Goods goods = requireMine(goodsId);
        if (!GoodsStatus.OFF_SHELF.equals(goods.getStatus())) {
            throw new BusinessException("仅「已下架」商品可重新上架");
        }
        goods.setStatus(GoodsStatus.ON_SALE);
        goodsMapper.updateById(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String goodsId) {
        Goods goods = requireMine(goodsId);
        if (GoodsStatus.LOCKED.equals(goods.getStatus()) || GoodsStatus.SOLD.equals(goods.getStatus())) {
            throw new BusinessException("交易中/已售出的商品不可删除");
        }
        // @TableLogic 逻辑删除（deleted=1），列表/详情查询自动过滤
        goodsMapper.deleteById(goodsId);
    }

    /** 查询商品并校验归属当前登录用户（卖家本人操作前置校验） */
    private Goods requireMine(String goodsId) {
        String sellerId = UserContext.requireUserId();
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在或已删除");
        }
        if (!sellerId.equals(goods.getSellerId())) {
            throw new BusinessException("只能操作自己发布的商品");
        }
        return goods;
    }
}
