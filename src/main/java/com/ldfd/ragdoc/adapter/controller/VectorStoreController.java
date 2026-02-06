package com.ldfd.ragdoc.adapter.controller;


import com.ldfd.ragdoc.application.VectorStoreService;
import com.ldfd.ragdoc.application.vo.VectorStoreVo;
import com.ldfd.ragdoc.domain.bo.VectorStore;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vectors")
@RequiredArgsConstructor
public class VectorStoreController {

    private final VectorStoreService vectorStoreService;

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable UUID id) {
        this.vectorStoreService.deleteById(id);
    }

    @GetMapping
    public List<VectorStoreVo> listUserVectors() {
        return this.vectorStoreService.listUserVectors();
    }
}
