package com.alioth.lingtu.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alioth.lingtu.client.LingtuChatClient;
import com.alioth.lingtu.common.BaseResult;
import com.alioth.lingtu.common.ResultUtils;
import com.alioth.lingtu.constant.LoginUserConstant;
import com.alioth.lingtu.model.reponse.ChatMessageResponse;
import com.alioth.lingtu.model.reponse.TravelChatResponse;
import com.alioth.lingtu.model.request.ChatRequest;
import com.alioth.lingtu.service.chat.ChatConversationService;
import com.alioth.lingtu.tools.ClientIpResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/travel")
@RequiredArgsConstructor
@Tag(name = "travelAssistant", description = "国内 RAG + 按出发地规划 + 天气工具 + 可选 MCP")
// @SecurityRequirement(name = OpenApiSecurityConfig.SECURITY_SCHEME_NAME)
public class LingtuAssistantController {

    private final LingtuChatClient lingtuChatClient;
    private final ChatConversationService chatConversationService;

    private final ToolCallback[] allTools;
    private final ObjectProvider<ToolCallbackProvider> toolCallbackProvider;

    private final ChatModel ollamaChatModel;

    private final ClientIpResolver clientIpResolver;


    // @Operation(
    //         summary = "旅游助手对话",
    //         description = "需登录：请求头 Authorization: Bearer {token}。可传 departureCity、tripDays、tripFocus；结合 RAG 与工具调用。"
    // )
    // @PostMapping("/chat")
    // public BaseResult<TravelChatResponse> chat(
    //         @RequestAttribute(LoginUserConstant.REQUEST_ATTR_USER_ID) Long loginUserId,
    //         HttpServletRequest httpServletRequest,
    //         @RequestBody ChatRequest request) {
    //     String chatId = request.getChatId();
    //     if (StrUtil.isBlank(chatId)) {
    //         chatId = UUID.randomUUID().toString();
    //     }
    //     String memoryConversationId = loginUserId + ":" + chatId;
    //     String clientIp = clientIpResolver.resolveClientIp(httpServletRequest);
    //     String answer = lingtuChatClient.chatWithTools(request.getMessage(), memoryConversationId, clientIp);
    //     TravelChatResponse response = new TravelChatResponse();
    //     return ResultUtils.success(response.chatId(chatId).answer(answer));
    // }


       @Operation(
               summary = "旅游助手对话SSE",
               description = "需登录：请求头 Authorization: Bearer {token}。可传 departureCity、tripDays、tripFocus；结合 RAG 与工具调用。"
       )
       @PostMapping(value = "/chat/sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
       public Flux<String> chatSSE(
               @RequestAttribute(LoginUserConstant.REQUEST_ATTR_USER_ID) Long loginUserId,
               @RequestBody ChatRequest request) {
           String chatId = request.getChatId();
           if (StrUtil.isBlank(chatId)) {
               chatId = UUID.randomUUID().toString();
           }
           String memoryConversationId = loginUserId + ":" + chatId;
           return lingtuChatClient.chatWithDashscopeBySse(request.getMessage(), memoryConversationId);
       }

    // @Operation(
    //         summary = "旅游助手对话sseEmitter",
    //         description = "需登录：请求头 Authorization: Bearer {token}。可传 departureCity、tripDays、tripFocus；结合 RAG 与工具调用。"
    // )
    // @PostMapping(value = "/chat/sse_emitter")
    // public SseEmitter chatSseEmitter(
    //         @RequestAttribute(LoginUserConstant.REQUEST_ATTR_USER_ID) Long loginUserId,
    //         HttpServletRequest httpServletRequest,
    //         @RequestBody ChatRequest request) {
    //     String chatId = request.getChatId();
    //     if (StrUtil.isBlank(chatId)) {
    //         chatId = UUID.randomUUID().toString();
    //     }
    //     String memoryConversationId = loginUserId + ":" + chatId;
    //     String clientIp = clientIpResolver.resolveClientIp(httpServletRequest);
    //     //获取Flux 响应式数据流 且通过  订阅 推送给 SseEmitter
    //     SseEmitter sseEmitter = new SseEmitter(180000L);
    //     lingtuChatClient.doChatWithMCPSSE(request.getMessage(), memoryConversationId, clientIp)
    //             .subscribe(chunk -> {
    //                 try {
    //                     sseEmitter.send(chunk);
    //                 } catch (IOException e) {
    //                     sseEmitter.completeWithError(e);
    //                 }
    //             }, sseEmitter::completeWithError, sseEmitter::complete);
    //     return sseEmitter;
    // }

    // @Operation(
    //         summary = "流式调用超级智能体",
    //         description = "需登录：请求头 Authorization: Bearer {token}。"
    // )
    // @PostMapping(value = "/manus/chat")
    // public SseEmitter chatManus(
    //         @RequestAttribute(LoginUserConstant.REQUEST_ATTR_USER_ID) Long loginUserId,
    //         @RequestBody ChatRequest request) {
    //     ProdigalManus manus = new ProdigalManus(
    //             allTools,
    //             toolCallbackProvider.getIfAvailable(),
    //             ollamaChatModel
    //     );
    //     return manus.runStream(request.getMessage());
    // }

    @Operation(
            summary = "历史会话",
            description = "不传 chatId 时返回当前用户全部会话（仅元数据）；传 chatId 时返回该会话及全部消息。"
    )
    @GetMapping("/conversations")
    public BaseResult<List<ChatMessageResponse>> conversations(
            @RequestAttribute(LoginUserConstant.REQUEST_ATTR_USER_ID) Long loginUserId,
            @RequestParam(required = false) String chatId) {
        return ResultUtils.success(chatConversationService.findByUserId(chatId, loginUserId));
    }

    @Operation(summary = "删除会话", description = "删除指定会话及其消息；需登录")
    @DeleteMapping("/conversations")
    public BaseResult<Void> deleteConversation(
            @RequestAttribute(LoginUserConstant.REQUEST_ATTR_USER_ID) Long loginUserId,
            @RequestParam String chatId) {
        chatConversationService.remove(chatId, loginUserId);
        return ResultUtils.success(null);
    }
}
