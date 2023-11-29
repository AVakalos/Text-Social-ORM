package org.apostolis.users.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.App;
import org.apostolis.security.TokenManager;
import org.apostolis.users.adapter.out.persistence.UserId;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.apostolis.users.domain.UserDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// Handles follows views http requests
public class FollowsViewController {

    private final GetFollowersAndUsersToFollowUseCase getFollowersAndUsersToFollowUseCase;

    private final TokenManager tokenManager;

    public FollowsViewController(GetFollowersAndUsersToFollowUseCase getFollowersAndUsersToFollowUseCase, TokenManager tokenManager) {
        this.getFollowersAndUsersToFollowUseCase = getFollowersAndUsersToFollowUseCase;
        this.tokenManager = tokenManager;
    }

    public void getFollowers(Context ctx){
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);

        Map<UserId, UserDTO> results = getFollowersAndUsersToFollowUseCase.getFollowers(
                App.currentUserId.get(),0,Integer.MAX_VALUE);

        Map<String, Object> response = new HashMap<>();
        response.put("message","User "+user+" followers");
        response.put("data",results);
        ctx.json(response);
    }

    public void getUsersToFollow(Context ctx){
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);

        Map<UserId, UserDTO> results = getFollowersAndUsersToFollowUseCase.getUsersToFollow(
                App.currentUserId.get(),0,Integer.MAX_VALUE);

        Map<String, Object> response = new HashMap<>();
        response.put("message","User "+user+" can follow");
        response.put("data",results);
        ctx.json(response);
    }
}
