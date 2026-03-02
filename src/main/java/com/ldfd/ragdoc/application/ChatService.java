package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.adapter.controller.dto.MessageDTO;
import com.ldfd.ragdoc.application.vo.MessageVo;
import com.ldfd.ragdoc.infrastructure.advisor.ToolAdvisor;
import com.ldfd.ragdoc.infrastructure.ai.tool.SearchTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import static com.ldfd.ragdoc.infrastructure.advisor.ChatHistoryAdvisor.USER_ID;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {

    private final AuthService authService;
    private final ChatClient chatJdbcClient;
    private final MDocService mDocService;
    private final Advisor retrievalTransAdvisor;
    private final SearchTool searchTool;

    /**
     * 多轮对话 - 在指定会话中发送消息
     */
    public MessageVo chat(String message, String conversationId) {
        String ret = chatJdbcClient.prompt()
                .user(message)
                .advisors(a -> a.param(CONVERSATION_ID, conversationId))
                .call()
                .content();

        return MessageVo.builder()
                .sessionId(conversationId)
                .content(ret)
                .build();
    }

    /**
     * 流式对话 - 在指定会话中发送消息并流式返回
     */
    public Flux<MessageVo> chatStream(MessageDTO message) {
        String conversationId = message.getSessionId();
        String content = message.getContent();
        Boolean useRetrieval = message.getUseRetrieval();
        Long userId = authService.getUserId();

        var prompt = chatJdbcClient.prompt()
                .user(content)
                .advisors(a -> a.param(USER_ID, userId));
        String filterExpr = "userId == " + userId;
        // 添加检索顾问（并传入用户ID参数）
        if (Boolean.TRUE.equals(useRetrieval)) {
            prompt = prompt.advisors(retrievalTransAdvisor)
                    .advisors(a -> a.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, filterExpr));
        }

        // 再添加会话记忆顾问
        prompt = prompt.advisors(a -> a.param(CONVERSATION_ID, conversationId));

        return prompt.stream()
                .content()
                .map(chunk -> MessageVo.builder()
                        .sessionId(conversationId)
                        .content(chunk)
                        .build());
    }

    /**
     * 文档总结 - 并行流式返回计划和Mermaid思维导图
     * Agent 1 和 Agent 2 并行执行，结果流式返回
     *
     * @param docId 文档ID
     * @param sessionId 会话ID
     * @param userMessage 用户的原始问题（用于保存到聊天记录）
     */
    public Flux<MessageVo> summaryDocuments(Long docId, String sessionId, String userMessage) {
        var docVo = mDocService.getById(docId);

        String initialInput = String.format("文档标题: %s\n\n文档内容:\n%s", docVo.getTitle(), docVo.getContent());

        // Agent 1: 分析文档，生成结构化计划（流式）
        String planPrompt = "请分析以下文档，生成结构化的总结计划。要求：识别主题、提取关键概念、列出3-7个主要分类和子要点。\n\n" + initialInput;

        Flux<MessageVo> planFlux = Flux.just(MessageVo.builder()
                        .sessionId(sessionId)
                        .content("## 📝 文档总结计划\n\n")
                        .build())
                .concatWith(chatJdbcClient.prompt()
                        .user(planPrompt)
                        .stream()
                        .content()
                        .map(chunk -> MessageVo.builder()
                                .sessionId(sessionId)
                                .content(chunk)
                                .build()));

        // Agent 2: 生成Mermaid思维导图（流式）
        String mermaidPrompt = """
                请根据文档内容生成**可直接运行的Mermaid代码**，无需额外解释，仅输出代码块（含```mermaid标识）：
                1. 图表类型：mindmap思维导图
                2. 核心主题：文档的核心内容
                3. 关键要素：文档的主要知识点和概念
                4. 输出要求：仅输出```mermaid + 代码 + ```，无多余文字、注释和说明。
                
                """ + initialInput;

        Flux<MessageVo> mermaidFlux = Flux.just(MessageVo.builder()
                        .sessionId(sessionId)
                        .content("\n\n## 🗺️ 思维导图\n\n")
                        .build())
                .concatWith(chatJdbcClient.prompt()
                        .user(mermaidPrompt)
                        .stream()
                        .content()
                        .map(chunk -> MessageVo.builder()
                                .sessionId(sessionId)
                                .content(chunk)
                                .build()));

        // 并行执行两个 Agent，合并流式结果
        // 使用 concat 按顺序输出：先计划，再思维导图
        // 如果要真正并行交错输出，可改用 Flux.merge(planFlux, mermaidFlux)
        return Flux.concat(planFlux, mermaidFlux);
    }

    /**
     * 根据用户输入的关键字，通过plan拆解和搜索tool调用，相关知识的讲解
     * 使用 Spring AI 工具集成方式，AI 会自动调用搜索工具获取相关信息
     *
     * @param topic 用户输入的主题
     * @param sessionId 会话ID
     * @return 流式返回内容
     */
    public Flux<MessageVo> explainTopic(String topic, String sessionId) {
        Long userId = authService.getUserId();

        String promptTemplate = """
                你是一个知识渊博的助手，专门为用户提供深入的主题讲解。请按照以下步骤操作：
                
                1. 分析用户输入的主题，拆解成3-5个关键子主题。
                2. 对每个子主题，调用【搜索工具】进行互联网搜索，获取相关最新信息。
                3. 综合搜索结果，为用户生成一篇结构清晰、内容详实的讲解文章，涵盖所有子主题。
                   - 每个子主题应包含 2-3 段详细说明
                   - 突出关键概念和要点
                   - 引用搜索结果中的具体信息
                
                用户输入的主题是：%s
                """;

        String fullPrompt = String.format(promptTemplate, topic);

        // 配置工具调用顾问，使 AI 能够自动调用搜索工具
        ToolAdvisor toolAdvisor = ToolAdvisor.builder()
                .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 300)
                .build();

        return chatJdbcClient.prompt()
                .user(fullPrompt)
                .advisors(a -> a.param(CONVERSATION_ID, sessionId))
                .advisors(a -> a.param(USER_ID, userId))
                .advisors(toolAdvisor)
                .tools(searchTool)  // 注册搜索工具，AI 会自动调用
                .stream()
                .content()
                .map(chunk -> MessageVo.builder()
                        .sessionId(sessionId)
                        .content(chunk)
                        .build());
    }

    /**
     * explainTopic 的同步版本（非流式）
     */
    public MessageVo explainTopicSync(String topic, String sessionId) {
        Long userId = authService.getUserId();

        String promptTemplate = """
                你是一个知识渊博的助手，专门为用户提供深入的主题讲解。请按照以下步骤操作：
                
                1. 分析用户输入的主题，拆解成3-5个关键子主题。
                2. 对每个子主题，调用【搜索工具】进行互联网搜索，获取相关最新信息。
                3. 综合搜索结果，为用户生成一篇结构清晰、内容详实的讲解文章，涵盖所有子主题。
                   - 每个子主题应包含 2-3 段详细说明
                   - 突出关键概念和要点
                   - 引用搜索结果中的具体信息
                
                用户输入的主题是：%s
                """;

        String fullPrompt = String.format(promptTemplate, topic);

        // 配置工具调用顾问，使 AI 能够自动调用搜索工具
        ToolCallAdvisor toolAdvisor = ToolCallAdvisor.builder()
                .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 300)
                .build();

        String content = chatJdbcClient.prompt()
                .user(fullPrompt)
                .advisors(a -> a.param(CONVERSATION_ID, sessionId))
                .advisors(a -> a.param(USER_ID, userId))
                .advisors(new SimpleLoggerAdvisor())
                .advisors(toolAdvisor)
                .tools(searchTool)  // 注册搜索工具，AI 会自动调用
                .call()
                .content();

        return MessageVo.builder()
                .sessionId(sessionId)
                .content(content)
                .build();
    }


}
