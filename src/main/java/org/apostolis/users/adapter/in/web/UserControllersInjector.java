package org.apostolis.users.adapter.in.web;

import org.apostolis.security.TokenManager;
import org.apostolis.users.application.ports.in.FollowsUseCase;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.apostolis.users.application.ports.in.LoginUseCase;
import org.apostolis.users.application.ports.in.RegisterUseCase;

public class UserControllersInjector {
    private final FollowsUseCase followsService;

    private final RegisterUseCase registerService;

    private final LoginUseCase loginService;

    private final GetFollowersAndUsersToFollowUseCase getFollowsService;

    private final TokenManager tokenManager;


    public UserControllersInjector(FollowsUseCase followsService,
                                   RegisterUseCase registerService,
                                   LoginUseCase loginService,
                                   GetFollowersAndUsersToFollowUseCase getFollowsService,
                                   TokenManager tokenManager) {
        this.followsService = followsService;
        this.registerService = registerService;
        this.loginService = loginService;
        this.getFollowsService = getFollowsService;
        this.tokenManager = tokenManager;
    }

    public AccountController getAccountController(){
        return new AccountController(registerService, loginService);
    }

    public FollowsController getFollowsController(){
        return new FollowsController(followsService,tokenManager);
    }

    public GetFollowsController getGetFollowsController(){
        return new GetFollowsController(getFollowsService,tokenManager);
    }
}
