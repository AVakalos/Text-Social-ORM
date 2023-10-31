package org.apostolis.users.adapter.in.web;

import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.InternalServerErrorResponse;
import org.apostolis.security.TokenManager;
import org.apostolis.users.application.ports.in.LoginUseCase;
import org.apostolis.users.application.ports.in.LoginCommand;
import org.apostolis.users.application.ports.in.RegisterUseCase;
import org.apostolis.users.application.ports.in.RegisterCommand;
import org.apostolis.users.domain.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountController {

    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    public AccountController(RegisterUseCase registerUseCase, LoginUseCase loginUseCase) {
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
    }

    public void signup(Context ctx){

        try {
            RegisterCommand registerCommand = ctx.bodyAsClass(RegisterCommand.class);
            registerUseCase.registerUser(registerCommand);
            ctx.result("User registered successfully!");
        }catch(ValueInstantiationException c) {
            throw new BadRequestResponse("User was not registered!\n" + c.getCause().getMessage());
        }
        catch(Exception e){
            throw new InternalServerErrorResponse("User was not registered!\n"+e.getMessage());
        }
    }

    public void login(Context ctx){
        try {
            LoginCommand loginCommand = ctx.bodyAsClass(LoginCommand.class);
            String token = loginUseCase.loginUser(loginCommand);
            ctx.json(new AuthResponse(loginCommand.username(), token));
        }catch(ValueInstantiationException c){
            throw new BadRequestResponse("User login failed!\n"+c.getCause().getMessage());
        }
        catch (Exception e) {
            throw new InternalServerErrorResponse("User login failed!\n"+e.getMessage());
        }
    }

    public void authenticate(Context ctx){
        String token = ctx.header("Authorization");
        try{
            loginUseCase.authenticate(token);
        }catch(Exception e){
            throw new BadRequestResponse(e.getMessage());
        }
    }
}