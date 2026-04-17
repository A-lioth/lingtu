package com.alioth.lingtu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {
        org.springframework.ai.model.chat.memory.repository.jdbc.autoconfigure.JdbcChatMemoryRepositoryAutoConfiguration.class,
})
public class LingtuApplication {

    public static void main(String[] args) {
        SpringApplication.run(LingtuApplication.class, args);
    }

}
