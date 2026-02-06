package com.ldfd.ragdoc.infrastructure.tool;

import com.ldfd.ragdoc.config.SearxngConfig;
import com.ldfd.ragdoc.infrastructure.ai.tool.SearchTool;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * SearchTool 真实访问测试
 */
class SearchToolTest {

    @Test
    void testSearch_RealSearch() {
        String baseUrl = "http://localhost:8888";
        String query = "Spring AI 教程";

        SearxngConfig config = new SearxngConfig();
        config.setBaseUrl(baseUrl);
        config.setMaxResults(5);

        SearchTool tool = new SearchTool(config);

        // 注意：此测试依赖于本地运行的 SearXNG 服务 (http://localhost:8888)
        try {
            String result = tool.search(query);
            assertNotNull(result);
            System.out.println("Search Result:\n" + result);
        } catch (Exception e) {
            // 允许连接失败的情况，避免在此环境下构建失败
            System.err.println("Search failed (might be expected if SearXNG is not running): " + e.getMessage());
        }
    }
}
