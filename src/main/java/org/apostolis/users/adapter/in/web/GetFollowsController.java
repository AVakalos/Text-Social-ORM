package org.apostolis.users.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.App;
import org.apostolis.security.TokenManager;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class GetFollowsController {

    private final GetFollowersAndUsersToFollowUseCase getFollowersAndUsersToFollowUseCase;

    private final TokenManager tokenManager;

    public GetFollowsController(GetFollowersAndUsersToFollowUseCase getFollowersAndUsersToFollowUseCase, TokenManager tokenManager) {
        this.getFollowersAndUsersToFollowUseCase = getFollowersAndUsersToFollowUseCase;
        this.tokenManager = tokenManager;
    }

    public void getFollowers(Context ctx){
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);

        HashMap<Long, String> results = getFollowersAndUsersToFollowUseCase.getFollowers(App.currentUserId.get(),0,Integer.MAX_VALUE);
        ctx.result(user+" followers: "+results.toString());

    }

    public void getUsersToFollow(Context ctx){
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);

        HashMap<Long, String> results = getFollowersAndUsersToFollowUseCase.getUsersToFollow(App.currentUserId.get(),0,Integer.MAX_VALUE);
        ctx.result(user+" can follow: "+results.toString());
    }
}
