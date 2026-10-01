package com.campus.cycle.service;

import com.campus.cycle.dto.SendMessageDTO;
import com.campus.cycle.entity.Message;
import com.campus.cycle.vo.ConversationRowVO;

import java.util.List;

/**
 * 站内沟通服务
 */
public interface ChatService {

    /** 会话列表（按角色联查对端用户与商品，按最后消息时间倒序） */
    List<ConversationRowVO> conversations();

    /** 聊天记录（进入会话即清空当前用户未读） */
    List<Message> messages(String conversationId);

    /** 发送消息（更新会话最后消息，对端未读 +1） */
    Message send(SendMessageDTO dto);
}
