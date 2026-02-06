package com.ldfd.ragdoc.adapter.controller;

import com.ldfd.ragdoc.adapter.common.Result;
import com.ldfd.ragdoc.adapter.controller.dto.LoginDTO;
import com.ldfd.ragdoc.application.vo.LoginVo;
import com.ldfd.ragdoc.adapter.controller.dto.RegisterDTO;
import com.ldfd.ragdoc.application.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<LoginVo> register(@Valid @RequestBody RegisterDTO request) {
        return new Result<>(authService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginDTO request) {
        return new Result<>(authService.login(request));
    }
}
