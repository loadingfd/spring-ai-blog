package com.ldfd.ragdoc.infrastructure.ai.tool.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SearXNG 搜索响应数据对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearxngResponse {

    private String query;

    @JsonProperty("number_of_results")
    private Integer numberOfResults;

    private List<SearchResult> results;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {
        private String url;
        private String title;
        private String content;
        private String engine;

        @JsonProperty("parsed_url")
        private List<String> parsedUrl;

        private String category;
    }
}
