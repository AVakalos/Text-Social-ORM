package org.apostolis.users.adapter.in.web;

import io.javalin.http.BadRequestResponse;
import org.apostolis.App;
import org.apostolis.security.TokenManager;
import org.apostolis.users.application.ports.in.FollowsCommand;
import org.apostolis.users.application.ports.in.FollowsUseCase;

import io.javalin.http.Context;


import java.util.Objects;

// Handles follow relationships management http requests
public class FollowsController{
    private final FollowsUseCase followsUseCase;
    private final TokenManager tokenManager;

    public FollowsController(FollowsUseCase followsUseCase, TokenManager tokenManager) {
        this.followsUseCase = followsUseCase;
        this.tokenManager = tokenManager;
    }

    public void follow(Context ctx){
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);
        Long follows;
        try{
            follows = ctx.queryParamAsClass("follows", Long.class).get();
        }catch(Exception k){
            throw new BadRequestResponse("follows query parameter must be an integer");
        }
        FollowsCommand followsCommand = new FollowsCommand(App.currentUserId.get().getUser_id(),follows);
        followsUseCase.followUser(followsCommand);
        ctx.result("User: "+user+ " followed user: "+follows);
    }

    public void unfollow(Context ctx){
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);
        Long unfollows;
        try{
            unfollows = ctx.queryParamAsClass("unfollows", Long.class).get();
        }catch(Exception k){
            throw new BadRequestResponse("unfollows query parameter must be an integer");
        }
        FollowsCommand followsCommand = new FollowsCommand(App.currentUserId.get().getUser_id(),unfollows);
        followsUseCase.unfollowUser(followsCommand);
        ctx.result("User: "+user+ " unfollowed user: "+unfollows);
    }
}
