package com.campus.cycle.vo;

import com.campus.cycle.entity.Conversation;
import com.campus.cycle.entity.Goods;
import lombok.Data;

/**
 * 会话列表行（对齐前端 ConversationRow 结构）
 */
@Data
public class ConversationRowVO {

    private Conversation conversation;
    private Goods goods;

    /** 对端用户 */
    private UserProfileVO peer;

    /** 当前用户未读数 */
    private Integer unread;
}
