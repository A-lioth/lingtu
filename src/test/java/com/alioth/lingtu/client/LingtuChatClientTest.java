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
        String chat = lingtuChatClient.chat("你好，我叫 alioth，请你记住我的名字", id);
        log.info("chat: {}", chat);
        String chat1 = lingtuChatClient.chat("我叫什么名字来着？", id);
        log.info("chat1: {}", chat1);
    }
}
