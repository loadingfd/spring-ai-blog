package com.ldfd.ragdoc.adapter.controller;

import com.ldfd.ragdoc.adapter.common.Result;
import com.ldfd.ragdoc.application.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rag")
@RequiredArgsConstructor
@Validated
public class RagController {

    private final RagService ragService;

    @PostMapping("/{id}/preprocess")
    public Result<Void> preProcessMarkdown(@PathVariable("id") Long id) {
        ragService.preProcessMarkdown(id);
        return new Result<>(null);
    }

}
