package com.campus.cycle.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.cycle.common.exception.BusinessException;
import com.campus.cycle.common.result.ResultCode;
import com.campus.cycle.common.util.Assemblers;
import com.campus.cycle.dto.SendMessageDTO;
import com.campus.cycle.entity.Conversation;
import com.campus.cycle.entity.Goods;
import com.campus.cycle.entity.Message;
import com.campus.cycle.entity.User;
import com.campus.cycle.mapper.ConversationMapper;
import com.campus.cycle.mapper.GoodsMapper;
import com.campus.cycle.mapper.MessageMapper;
import com.campus.cycle.mapper.UserMapper;
import com.campus.cycle.security.UserContext;
import com.campus.cycle.service.ChatService;
import com.campus.cycle.service.SchoolService;
import com.campus.cycle.vo.ConversationRowVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内沟通服务
 */
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final GoodsMapper goodsMapper;
    private final UserMapper userMapper;
    private final SchoolService schoolService;

    @Override
    public List<ConversationRowVO> conversations() {
        String uid = UserContext.requireUserId();
        List<Conversation> list = conversationMapper.selectList(Wrappers.<Conversation>lambdaQuery()
                .and(w -> w.eq(Conversation::getBuyerId, uid).or().eq(Conversation::getSellerId, uid))
                .orderByDesc(Conversation::getLastTime));
        return list.stream()
                .map(c -> {
                    String peerId = uid.equals(c.getBuyerId()) ? c.getSellerId() : c.getBuyerId();
                    User peer = userMapper.selectById(peerId);
                    Goods goods = goodsMapper.selectById(c.getGoodsId());
                    if (peer == null || goods == null) {
                        return null;
                    }
                    ConversationRowVO vo = new ConversationRowVO();
                    vo.setConversation(c);
                    vo.setGoods(goods);
                    vo.setPeer(Assemblers.toProfile(peer, schoolService.nameOf(peer.getSchoolId())));
                    Map<String, Integer> unreadFor = c.getUnreadFor();
                    vo.setUnread(unreadFor == null ? 0 : unreadFor.getOrDefault(uid, 0));
                    return vo;
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    public List<Message> messages(String conversationId) {
        String uid = UserContext.requireUserId();
        Conversation conversation = requireParticipant(conversationId, uid);
        // 进入会话即清空当前用户未读
        if (conversation.getUnreadFor() != null && conversation.getUnreadFor().getOrDefault(uid, 0) > 0) {
            conversation.getUnreadFor().put(uid, 0);
            conversationMapper.updateById(conversation);
        }
        return messageMapper.selectList(Wrappers.<Message>lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message send(SendMessageDTO dto) {
        String uid = UserContext.requireUserId();
        Conversation conversation = requireParticipant(dto.getConversationId(), uid);

        Message message = new Message();
        message.setConversationId(conversation.getId());
        message.setFromUserId(uid);
        message.setContent(dto.getContent());
        message.setTime(System.currentTimeMillis());
        messageMapper.insert(message);

        // 更新会话最后消息
        conversation.setLastMessage(dto.getContent());
        conversation.setLastTime(message.getTime());
        // 对端未读 +1
        String peerId = uid.equals(conversation.getBuyerId()) ? conversation.getSellerId() : conversation.getBuyerId();
        Map<String, Integer> unreadFor = conversation.getUnreadFor() == null ? new HashMap<>() : conversation.getUnreadFor();
        unreadFor.merge(peerId, 1, Integer::sum);
        conversation.setUnreadFor(unreadFor);
        conversationMapper.updateById(conversation);
        return message;
    }

    /** 校验当前用户是该会话参与者 */
    private Conversation requireParticipant(String conversationId, String uid) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "会话不存在");
        }
        if (!uid.equals(conversation.getBuyerId()) && !uid.equals(conversation.getSellerId())) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权访问该会话");
        }
        return conversation;
    }
}
