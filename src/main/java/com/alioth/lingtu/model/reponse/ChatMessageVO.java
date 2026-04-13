package com.alioth.lingtu.model.reponse;

import com.alioth.lingtu.model.entity.ChatMessage;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 会话消息 VO
 *
 * @author alioth
 */
@Data
@Builder
public class ChatMessageVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String conversationId;

    private String title;

    private List<ChatMessage> messages;

    /**
     * 会话最近活动时间（用于前端历史分组、排序），一般对应库表 update_time。
     */
    private Date updateTime;

}
