package com.campus.cycle.controller;

import com.campus.cycle.common.result.Result;
import com.campus.cycle.dto.SendMessageDTO;
import com.campus.cycle.entity.Message;
import com.campus.cycle.service.ChatService;
import com.campus.cycle.vo.ConversationRowVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 站内沟通接口
 */
@Tag(name = "站内沟通")
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(summary = "会话列表（按角色联查对端用户与商品）")
    @GetMapping("/conversations")
    public Result<List<ConversationRowVO>> conversations() {
        return Result.success(chatService.conversations());
    }

    @Operation(summary = "聊天记录（进入会话即清空当前用户未读）")
    @GetMapping("/messages")
    public Result<List<Message>> messages(@RequestParam String conversationId) {
        return Result.success(chatService.messages(conversationId));
    }

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public Result<Message> send(@Valid @RequestBody SendMessageDTO dto) {
        return Result.success(chatService.send(dto));
    }
}
