package org.apostolis.users.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.users.application.ports.in.*;

import java.util.HashMap;
import java.util.Map;

// Handles account http requests and user authentication
public class AccountController {
    private final AccountManagementUseCase accountService;

    public AccountController(AccountManagementUseCase accountService) {
        this.accountService = accountService;
    }


    public void signup(Context ctx){
        RegisterCommand registerCommand = ctx.bodyAsClass(RegisterCommand.class);
        accountService.registerUser(registerCommand);
        ctx.result("User registered successfully!");
    }

    public void login(Context ctx) {
        LoginCommand loginCommand = ctx.bodyAsClass(LoginCommand.class);
        String token = accountService.loginUser(loginCommand);
        Map<String, Object> response = new HashMap<>();
        response.put("username",loginCommand.username());
        response.put("token",token);
        ctx.json(response);
    }

    public void authenticate(Context ctx){
        accountService.authenticate(ctx.header("Authorization"));
    }
}