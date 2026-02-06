package com.ldfd.ragdoc.application;

import com.ldfd.ragdoc.adapter.controller.dto.MessageDTO;
import com.ldfd.ragdoc.application.vo.MessageVo;
import com.ldfd.ragdoc.infrastructure.advisor.ChatHistoryAdvisor;
import com.ldfd.ragdoc.infrastructure.advisor.ToolAdvisor;
import com.ldfd.ragdoc.infrastructure.ai.tool.SearchTool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

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
     * 文档总结 - 通过链式Agent生成计划、Mermaid思维导图和文字总结
     * 计划和Mermaid并行执行（同步调用），总结最后流式返回
     *
     * @param docId 文档ID
     * @param sessionId 会话ID
     * @param userMessage 用户的原始问题（用于保存到聊天记录）
     */
    public Flux<MessageVo> summaryDocuments(Long docId, String sessionId, String userMessage) {
        Long userId = authService.getUserId();
        var docVo = mDocService.getById(docId);

        // 定义并行Agent的系统提示词（并行执行：计划 + Mermaid）
        String[] parallelPrompts = {
            // Agent 1: 分析文档，生成结构化计划
            "请分析以下文档，生成结构化的总结计划。要求：识别主题、提取关键概念、列出3-7个主要分类和子要点。",

            // Agent 2: 生成Mermaid思维导图
                """
                请根据文档内容和以下要求生成**可直接运行的Mermaid代码**，无需额外解释，仅输出代码块（含```mermaid标识）：
                1. 图表类型：【替换为具体类型，如：流程图/时序图/类图/状态图/甘特图/饼图】
                2. 核心主题：【替换为图表核心表达的内容，如：用户登录系统流程/订单支付时序/电商系统类关系】
                3. 关键要素：【分点列出图表核心节点/步骤/元素，如：1.用户输入账号密码 2.后台验证身份 3.验证成功跳转到首页】
                4. 样式要求（可选）：【替换为样式偏好，如：横向布局/简约风格/指定主色调/#165DFF/圆角矩形】
                5. 输出要求：仅输出```mermaid + 代码 + ```，无多余文字、注释和说明。
                
                ```mermaid
                # 【AI将在此处生成对应Mermaid代码】
                """
        };

        String initialInput = String.format("文档标题: %s\n\n文档内容:\n%s", docVo.getTitle(), docVo.getContent());

        // 并行执行计划和Mermaid生成（同步调用）
        // 注意：不使用 USER_ID advisor，避免保存中间转换的提示词到聊天记录
        List<String> parallelResults = new ParallelizationWorkflow(chatJdbcClient)
                .parallel(
                        initialInput,
                        Arrays.asList(parallelPrompts),
                        2
                );


        // 整合并行结果
        String plan = parallelResults.get(0);
        String mermaidDiagram = parallelResults.get(1);

        // 构建用户的原始问题（用于保存到聊天记录）
        String originalUserQuestion = userMessage != null && !userMessage.isBlank()
                ? userMessage
                : String.format("请总结文档《%s》的内容，生成文字总结和思维导图", docVo.getTitle());

        // 最后一个Agent：基于计划生成详细的文字总结（流式返回）
        // 将所有内容放在user消息中，确保AI能够正确处理
        String fullPrompt = String.format(
                """
                        请基于以下信息生成文档总结，并按以下格式输出：
                        1. 首先输出「## 📝 文档总结」标题
                        2. 然后根据总结计划生成详细的文字总结内容（结构清晰、每部分2-3段说明、突出关键概念、500-800字）
                        3. 接着输出「## 🗺️ 思维导图」标题
                        4. 最后输出以下Mermaid mindmap代码块（直接输出，不要修改）
                        
                        ===== 文档标题 =====
                        %s
                        
                        ===== 总结计划 =====
                        %s
                        
                        ===== Mermaid思维导图 =====
                        %s
                        
                        ===== 文档内容 =====
                        %s""",
            docVo.getTitle(),
            plan,
            mermaidDiagram,
            docVo.getContent()
        );

        // 流式返回最终结果
        // 将完整提示词放在user消息中，通过context传递原始用户问题用于保存聊天记录
        return chatJdbcClient.prompt()
                .user(fullPrompt)  // 完整的提示词（包含所有内容）
                .advisors(a -> a.param(ChatHistoryAdvisor.ORIGINAL_USER_MESSAGE, originalUserQuestion))  // 原始用户问题
                .advisors(a -> a.param(USER_ID, userId))
                .advisors(a -> a.param(CONVERSATION_ID, sessionId))
                .stream()
                .content()
                .map(chunk -> MessageVo.builder()
                        .sessionId(sessionId)
                        .content(chunk)
                        .build());
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
