package org.apostolis.users.adapter.in.web;

import io.javalin.http.BadRequestResponse;
import lombok.RequiredArgsConstructor;
import org.apostolis.App;
import org.apostolis.security.TokenManager;
import org.apostolis.users.application.ports.in.FollowsCommand;
import org.apostolis.users.application.ports.in.FollowsUseCase;

import io.javalin.http.Context;
import org.apostolis.users.domain.UserId;


import java.util.Objects;

// Handles follow relationships management http requests
@RequiredArgsConstructor
public class FollowsController{
    private final FollowsUseCase followsUseCase;
    private final TokenManager tokenManager;

    public void follow(Context ctx) throws Exception {
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);
        Long follows;
        try{
            follows = ctx.queryParamAsClass("follows", Long.class).get();
        }catch(Exception k){
            throw new BadRequestResponse("follows query parameter must be an integer");
        }
        FollowsCommand followsCommand = new FollowsCommand(App.currentUserId.get(),new UserId(follows));
        followsUseCase.followUser(followsCommand);
        ctx.result("User: "+user+ " followed user: "+follows);
    }

    public void unfollow(Context ctx) throws Exception {
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);
        Long unfollows;
        try{
            unfollows = ctx.queryParamAsClass("unfollows", Long.class).get();
        }catch(Exception k){
            throw new BadRequestResponse("unfollows query parameter must be an integer");
        }
        FollowsCommand followsCommand = new FollowsCommand(App.currentUserId.get(),new UserId(unfollows));
        followsUseCase.unfollowUser(followsCommand);
        ctx.result("User: "+user+ " unfollowed user: "+unfollows);
    }
}
