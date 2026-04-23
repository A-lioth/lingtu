package com.alioth.lingtu.service.chat;

import com.alioth.lingtu.model.entity.ChatConversation;
import com.alioth.lingtu.model.reponse.ChatMessageResponse;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @author alioth
 * @description 针对表【chat_conversation(会话表)】的数据库操作Service
 * @createDate 2026-04-04 15:49:24
 */
public interface ChatConversationService extends IService<ChatConversation> {

    List<ChatMessageResponse> findByUserId(String conversationId, Long userId);

    void remove(String conversationId, Long userId);
}
