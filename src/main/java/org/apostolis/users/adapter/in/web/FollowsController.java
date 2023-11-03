package org.apostolis.users.adapter.in.web;

import io.javalin.http.BadRequestResponse;
import org.apostolis.security.TokenManager;
import org.apostolis.users.application.ports.in.FollowsCommand;
import org.apostolis.users.application.ports.in.FollowsUseCase;

import io.javalin.http.Context;


import java.util.Objects;

public class FollowsController{
    private final FollowsUseCase followsUseCase;
    private final TokenManager tokenManager;

    public FollowsController(FollowsUseCase followsUseCase, TokenManager tokenManager) {
        this.followsUseCase = followsUseCase;
        this.tokenManager = tokenManager;
    }

    // TODO: Usernames at responses

    public void follow(Context ctx){
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);
        int follows;
        try{
            follows = ctx.queryParamAsClass("follows", Integer.class).get();
        }catch(Exception k){
            throw new BadRequestResponse("follows query parameter must be an integer");
        }
        FollowsCommand followsCommand = new FollowsCommand(follows);
        followsUseCase.followUser(followsCommand);
        ctx.result("User: "+user+ " followed user: "+follows);
    }

    public void unfollow(Context ctx){
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);
        int unfollows;
        try{
            unfollows = ctx.queryParamAsClass("unfollows", Integer.class).get();
        }catch(Exception k){
            throw new BadRequestResponse("unfollows query parameter must be an integer");
        }
        FollowsCommand followsCommand = new FollowsCommand(unfollows);
        followsUseCase.unfollowUser(followsCommand);
        ctx.result("User: "+user+ " unfollowed user: "+unfollows);
    }
}
