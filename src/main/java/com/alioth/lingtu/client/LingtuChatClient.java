package com.alioth.lingtu.client;

import com.alioth.lingtu.advisor.LoggerAdvisor;
import com.alioth.lingtu.chatmemory.MySQLChatMemory;
import com.alioth.lingtu.constant.LingtuConstant;
import com.alioth.lingtu.rag.QueryRewriter;
import com.alioth.lingtu.rag.RagCustomAdvisorFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j
@Component
public class LingtuChatClient {

    @Qualifier("ollamaChatModel")
    @Resource
    private ChatModel ollamaChatModel;

    @Qualifier("dashscopeChatModel")
    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private ChatMemoryRepository jdbcChatMemoryRepository;

    private ChatClient ollamaChatClient;
    private ChatClient dashscopeChatClient;

    /**
     * 初始化 Ollama ChatClient - 基于内存的对话记忆
     */
    @PostConstruct
    public void initOllamaChatClient() {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();
        ollamaChatClient = ChatClient.builder(ollamaChatModel)
                .defaultSystem(LingtuConstant.SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志 Advisor,可按需开启
                        new LoggerAdvisor()
                        // 自定义推理增强 Advisor,可按需开启
                        // ,new ReReadingAdvisor()
                )
                .build();
    }

    @Resource
    private MySQLChatMemory chatMemory;

    /**
     * 初始化 DashScope ChatClient - 基于 MySQL 的会话记忆
     */
    @PostConstruct
    public void initDashscopeChatClient() {
        // MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
        //         .chatMemoryRepository(jdbcChatMemoryRepository)
        //         .maxMessages(20)
        //         .build();
        dashscopeChatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(LingtuConstant.SYSTEM_PROMPT)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), new LoggerAdvisor())
                .build();
    }

    /**
     * 聊天 - 使用 Ollama 模型
     *
     * @param question
     * @param chatId
     * @return
     */
    public String chatWithOllama(String question, String chatId) {
        ChatResponse chatResponse = ollamaChatClient.prompt()
                .user(question)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        return content;
    }

    /**
     * 聊天（SSE） - 使用 dashscope 模型
     *
     * @param question
     * @param chatId
     * @return
     */
    public Flux<String> chatWithDashscopeBySse(String question, String chatId) {
        return dashscopeChatClient.prompt()
                .user(question)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(allTools)
                .stream()
                .content();
    }

    @Resource
    private VectorStore pgVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    @Resource
    private ToolCallback[] allTools;

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
    public String chatWithRag(String message, String chatId) {
        //重写查询
        String rewriterMessage = queryRewriter.doRewriteQuery(message);

        // 对向量数据库中的所有文档执行相似性搜索 该与 RetrievalAugmentationAdvisor 检索功能重叠
        // 造成token 浪费
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(pgVectorStore)
                .searchRequest(SearchRequest.builder().similarityThreshold(0.7d).topK(3).build())
                .build();

        ChatResponse chatResponse = dashscopeChatClient.prompt()
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId)
                        .param("TOP_K", 10) //关联会话的条数
                )
                .user(rewriterMessage)
                //开启日志
                // .advisors(new LoggerAdvisor())
                //基于 pgVector 知识库回答
                // .advisors(questionAnswerAdvisor)
                //检索增强顾问
                .advisors(RagCustomAdvisorFactory.createRetrievalAugmentationAdvisor(pgVectorStore, dashscopeChatModel))
                //添加工具
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    /**
     * AI 恋爱报告功能（支持调用工具）- 使用 DashScope 模型
     *
     * @param message
     * @param chatId
     * @return
     */
    public String chatWithTools(String message, String chatId) {
        ChatResponse chatResponse = dashscopeChatClient
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
