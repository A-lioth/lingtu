package com.alioth.lingtu.client;

import com.alioth.lingtu.constant.LingtuConstant;
import com.alioth.lingtu.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LingtuChatClient {

    private final ChatClient chatClient;
    // private final ChatModel ollamaChatModel;

    /**
     * 基于内存的对话记忆
     */
    // public LingtuChatClient(ChatModel ollamaChatModel) {
    //     MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
    //             .chatMemoryRepository(new InMemoryChatMemoryRepository())
    //             .maxMessages(20)
    //             .build();
    //     chatClient = ChatClient.builder(ollamaChatModel)
    //             // .defaultSystem(SYSTEM_PROMPT)
    //             .defaultSystem(LingtuConstant.TEST_PROMPT)
    //             .defaultAdvisors(
    //                     MessageChatMemoryAdvisor.builder(chatMemory).build(),
    //                     // 自定义日志 Advisor，可按需开启
    //                     new SimpleLoggerAdvisor()
    //                     // 自定义推理增强 Advisor，可按需开启
    //                     //,new ReReadingAdvisor()
    //             )
    //             .build();
    // }

    /**
     * 基于 MySQL 的会话记忆
     */
    public LingtuChatClient(ChatModel ollamaChatModel, ChatMemoryRepository jdbcChatMemoryRepository) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(20)
                .build();
        chatClient = ChatClient.builder(ollamaChatModel)
                .defaultSystem(LingtuConstant.TEST_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
    public String chat(String question, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(question)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        return content;
    }

    // @Autowired
    // private ObjectProvider<VectorStore> pgVectorStore;
    // @Resource
    // private QueryRewriter queryRewriter;

    // public LingtuChatClient(ChatModel ollamaChatModel) {
    //     this.ollamaChatModel = ollamaChatModel;
    //     chatClient = ChatClient.builder(ollamaChatModel)
    //             // .defaultSystem(SYSTEM_PROMPT)
    //             .defaultSystem(LingtuConstant.TEST_PROMPT)
    //             // .defaultAdvisors(new MyLoggerAdvisor())
    //             .build();
    // }

    /**
     * 带 RAG 的对话-使用知识库回答
     * 1、重写查询
     * 2、基于 pgVector知识库回答
     * 3、基于 pgVector知识库的检索增强
     *
     * @param message 用户输入
     * @param chatId  当前会话Id
     * @return
     */
    // public String chatWithRag(String message, String chatId) {
    //     VectorStore store = pgVectorStore.getIfAvailable();
    //     if (store == null) {
    //         throw new IllegalStateException(
    //                 "VectorStore 未配置：请取消注释 PgVectorStoreConfig 的 @Configuration，并配置 PostgreSQL（pgvector）数据源。");
    //     }
    //     //重写查询
    //     String rewriterMessage = queryRewriter.doRewriteQuery(message);
    //
    //     //对向量数据库中的所有文档执行相似性搜索 该与 RetrievalAugmentationAdvisor 检索功能重叠
    //     //造成token 浪费
    //     QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder((VectorStore) pgVectorStore)
    //             .searchRequest(SearchRequest.builder().similarityThreshold(0.7d).topK(3).build())
    //             .build();
    //
    //     ChatResponse chatResponse = chatClient.prompt()
    //             .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId) //对话id
    //                     .param("TOP_K", 10) //关联会话的条数
    //             )
    //             .user(rewriterMessage)
    //             //开启日志
    //             // .advisors(new MyLoggerAdvisor())
    //             //基于 pgVector知识库回答
    //             .advisors(questionAnswerAdvisor)
    //             //检索增强顾问
    //             // .advisors(RagCustomAdvisorFactory.createRetrievalAugmentationAdvisor(store, ollamaChatModel))
    //             //添加工具
    //             // .toolCallbacks(allTools)
    //             .call()
    //             .chatResponse();
    //     return chatResponse.getResult().getOutput().getText();
    // }


    @Resource
    private ToolCallback[] allTools;

    /**
     * AI 恋爱报告功能（支持调用工具）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String chatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                // 开启日志，便于观察效果
                // .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
