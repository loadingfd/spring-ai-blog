package com.ldfd.ragdoc.adapter.controller;

import com.ldfd.ragdoc.adapter.common.Result;
import com.ldfd.ragdoc.adapter.controller.dto.UserDTO;
import com.ldfd.ragdoc.application.UserService;
import com.ldfd.ragdoc.application.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping
    public Result<UserVo> create(@RequestBody @Validated UserDTO request) {
        return new Result<>(userService.create(request));
    }

    @GetMapping("/{id}")
    public Result<UserVo> get(@PathVariable Long id) {
        return new Result<>(userService.getById(id));
    }

    @GetMapping
    public Result<List<UserVo>> list() {
        return new Result<>(userService.list());
    }

    @PutMapping("/{id}")
    public Result<UserVo> update(@PathVariable Long id, @RequestBody @Validated UserDTO request) {
        return new Result<>(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return new Result<>(null);
    }

}
