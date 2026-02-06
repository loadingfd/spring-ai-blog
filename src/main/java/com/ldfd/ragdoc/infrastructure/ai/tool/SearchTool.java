package com.ldfd.ragdoc.infrastructure.ai.tool;

import com.ldfd.ragdoc.config.SearxngConfig;
import com.ldfd.ragdoc.exception.BusinessException;
import com.ldfd.ragdoc.infrastructure.ai.tool.model.SearxngResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class SearchTool {

    private final SearxngConfig searxngConfig;
    private final RestClient searxngRestClient;

    public SearchTool(SearxngConfig searxngConfig) {
        this.searxngConfig = searxngConfig;
        this.searxngRestClient = RestClient.builder()
                .baseUrl(searxngConfig.getBaseUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, "ragdoc/1.0")
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Tool(name = "web_search",
            description = """
            使用互联网搜索引擎进行实时网页搜索，获取最新信息、新闻、事实、数据、产品价格等。
            适用场景：
            - 查询当前事件、新闻、股价、天气、体育比分、最新研究
            - 验证事实、查找模型知识截止后的内容
            - 搜索产品规格、评论、价格对比
            - 需要外部实时数据的任何问题
            
            输入建议：
            - query 要写得具体、精确，例如 "2026 年最新 iPhone 型号和价格 中国" 而不是 "iPhone"
            输出格式：
            最多返回 5 条结果）。
            """)
    public String search(@ToolParam String query) {
        try {
            log.info("SearXNG 搜索请求: query={}", query);

            // 调用 SearXNG API
            SearxngResponse response = searxngRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", query)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .body(SearxngResponse.class);

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                log.warn("SearXNG 搜索无结果: query={}", query);
                return "未找到相关信息。";
            }

            // 限制返回结果数量
            int maxResults = searxngConfig.getMaxResults();
            var limitedResults = response.getResults().stream()
                    .limit(maxResults)
                    .toList();

            StringBuilder resultSummary = new StringBuilder();
            limitedResults.forEach(result -> {
                resultSummary.append("- ").append(result.getTitle())
                        .append(": ").append(result.getUrl()).append("\n");
            });

            log.debug("SearXNG 搜索成功: query={}, results={}", query, limitedResults.size());
            return resultSummary.toString();

        } catch (Exception e) {
            log.error("SearXNG 搜索失败: query={}, error={}", query, e.getMessage(), e);
            throw new BusinessException("搜索服务暂时不可用，请稍后再试。错误信息: " + e.getMessage(), e);
        }
    }
}
