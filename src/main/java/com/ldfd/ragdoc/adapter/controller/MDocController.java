package com.ldfd.ragdoc.adapter.controller;

import com.ldfd.ragdoc.adapter.common.Result;
import com.ldfd.ragdoc.adapter.controller.dto.MDocDTO;
import com.ldfd.ragdoc.application.MDocService;
import com.ldfd.ragdoc.application.vo.MDocVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mdoc")
@RequiredArgsConstructor
@Validated
public class MDocController {

    private final MDocService mDocService;

    @PostMapping
    public Result<MDocVo> create(@RequestBody @Validated MDocDTO request) {
        return new Result<>(mDocService.create(request));
    }

    @GetMapping("/{id}")
    public Result<MDocVo> get(@PathVariable Long id) {
        return new Result<>(mDocService.getById(id));
    }

    @GetMapping
    public Result<List<MDocVo>> list() {
        return new Result<>(mDocService.list());
    }

    @PutMapping("/{id}")
    public Result<MDocVo> update(@PathVariable Long id, @RequestBody @Validated MDocDTO request) {
        return new Result<>(mDocService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        mDocService.delete(id);
        return new Result<>(null);
    }

}
