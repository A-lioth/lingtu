package com.alioth.lingtu.client;

import com.alioth.lingtu.LingtuApplication;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest(classes = LingtuApplication.class)
class LingtuChatClientTest {

    @Resource
    private LingtuChatClient lingtuChatClient;

    @Test
    void chat() {
        String id = UUID.randomUUID().toString();
        String chat1 = lingtuChatClient.chat("你好，我叫 alioth", id);
        log.info("chat1: {}", chat1);
        String chat3 = lingtuChatClient.chat("你可以干什么呢？", id);
        log.info("chat3: {}", chat3);
    }

    @Test
    void chatWithRag() {
        String id = UUID.randomUUID().toString();
        // String chat = lingtuChatClient.chatWithRag("我想去贵州旅行，有什么推荐的吗？", id);
        // log.info("chatWithRag: {}", chat);
        String chat2 = lingtuChatClient.chatWithRag("我想去爬山，明天和朋友一起从银川出发，有什么推荐的吗？将详细计划以PDF的形式发给我的邮箱baker_20231793@qq.com", id);
        log.info("chatWithRag: {}", chat2);
    }

    @Test
    void chatWithTools() {
        //第一轮
        String chatId = cn.hutool.core.lang.UUID.fastUUID().toString();
        String content = lingtuChatClient.chatWithTools("规划一份在银川的旅行计划，将pdf发送给 3434039767@qq.com", chatId);
        assertNotNull(content);
    }
}
