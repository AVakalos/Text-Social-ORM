package org.apostolis.users.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.users.application.ports.in.LoginUseCase;
import org.apostolis.users.application.ports.in.LoginCommand;
import org.apostolis.users.application.ports.in.RegisterUseCase;
import org.apostolis.users.application.ports.in.RegisterCommand;
import org.apostolis.users.domain.AuthResponse;

public class AccountController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;

    public AccountController(RegisterUseCase registerUseCase, LoginUseCase loginUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
    }

    public void signup(Context ctx){
        RegisterCommand registerCommand = ctx.bodyAsClass(RegisterCommand.class);
        registerUseCase.registerUser(registerCommand);
        ctx.result("User registered successfully!");
    }

    public void login(Context ctx) {
        LoginCommand loginCommand = ctx.bodyAsClass(LoginCommand.class);
        String token = loginUseCase.loginUser(loginCommand);
        ctx.json(new AuthResponse(loginCommand.username(), token));
    }

    public void authenticate(Context ctx){
        loginUseCase.authenticate(ctx.header("Authorization"));
    }
}