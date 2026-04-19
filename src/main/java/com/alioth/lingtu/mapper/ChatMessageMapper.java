package com.alioth.lingtu.mapper;

import com.alioth.lingtu.model.entity.ChatMessage;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author alioth
* @description 针对表【chat_message(会话消息表)】的数据库操作Mapper
* @Entity com.alioth.lingtu.model.entity.ChatMessage
*/
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    default List<ChatMessage> findByConversationIdAndUserId(String conversationId, Long userId, int lastN){
        LambdaQueryWrapper<ChatMessage> wrapper = Wrappers.<ChatMessage>lambdaQuery()
                .eq(ChatMessage::getConversationId, conversationId)
                .eq(ChatMessage::getUserId, userId)
                .orderByDesc(ChatMessage::getCreateTime)
                .last("LIMIT " + lastN);
       return this.selectList(wrapper);
    }
}
