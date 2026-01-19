package com.ldfd.ragdoc.application;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RagService {

    private final OllamaChatModel ollamaChatModel;

}
