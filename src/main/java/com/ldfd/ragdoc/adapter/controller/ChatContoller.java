package com.ldfd.ragdoc.adapter.controller;

import com.ldfd.ragdoc.adapter.common.Result;
import com.ldfd.ragdoc.adapter.controller.dto.MessageDTO;
import com.ldfd.ragdoc.application.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatContoller {

    private final ChatService ragService;

    /**
     * 单轮对话（原有接口，保持兼容）
     */
    @PostMapping
    public Result<String> chat(@RequestBody MessageDTO message){
        String msg = message.getMessage();
        String reply = ragService.chat(msg);
        return new Result<>(reply);
    }


}
