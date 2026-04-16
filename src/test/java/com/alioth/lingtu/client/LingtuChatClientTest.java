package com.alioth.lingtu.client;

import com.alioth.lingtu.LingtuApplication;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

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
        String chat = lingtuChatClient.chatWithRag("我想去贵州旅行，有什么推荐的吗？", id);
        log.info("chatWithRag: {}", chat);
    }
}
