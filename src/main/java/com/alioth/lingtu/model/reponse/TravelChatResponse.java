package com.alioth.lingtu.model.reponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 旅游助手对话响应
 *
 * @author alioth
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "旅游助手对话响应")
public class TravelChatResponse {

    @Schema(description = "会话 id")
    private String chatId;

    @Schema(description = "模型回答")
    private String answer;


    public TravelChatResponse chatId(String chatId) {
        this.chatId = chatId;
        return this;
    }

    public TravelChatResponse answer(String answer) {
        this.answer = answer;
        return this;
    }
}
