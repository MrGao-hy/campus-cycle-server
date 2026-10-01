package com.campus.cycle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发送聊天消息请求
 */
@Data
public class SendMessageDTO {

    @NotBlank(message = "会话 ID 不能为空")
    private String conversationId;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 1000, message = "消息过长")
    private String content;
}
