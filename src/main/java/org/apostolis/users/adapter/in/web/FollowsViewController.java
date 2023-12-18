package org.apostolis.users.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.App;
import org.apostolis.security.TokenManager;
import org.apostolis.users.domain.FollowsView;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;

import java.util.*;

// Handles follows views http requests
public class FollowsViewController {

    private final GetFollowersAndUsersToFollowUseCase getFollowersAndUsersToFollowUseCase;

    private final TokenManager tokenManager;

    public FollowsViewController(GetFollowersAndUsersToFollowUseCase getFollowersAndUsersToFollowUseCase, TokenManager tokenManager) {
        this.getFollowersAndUsersToFollowUseCase = getFollowersAndUsersToFollowUseCase;
        this.tokenManager = tokenManager;
    }

    public void getFollowers(Context ctx) throws Exception {
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);

        FollowsView data = getFollowersAndUsersToFollowUseCase.getFollowers(
                App.currentUserId.get(),0,Integer.MAX_VALUE);

        Map<String, Object> response = new HashMap<>();
        response.put("message","User "+user+" followers");
        response.put("data",mapToDTO(data));
        ctx.json(response);
    }

    public void getUsersToFollow(Context ctx) throws Exception {
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);
        FollowsView data = getFollowersAndUsersToFollowUseCase.getUsersToFollow(
                App.currentUserId.get(),0,Integer.MAX_VALUE);

        Map<String, Object> response = new HashMap<>();
        response.put("message","User "+user+" can follow");
        response.put("data",mapToDTO(data));
        ctx.json(response);
    }

    private List<UserDTO> mapToDTO(FollowsView data){
        List<UserDTO> userDTOs = new ArrayList<>();
        for(UserId user_id: data.getUsers().getData().keySet()){
            userDTOs.add(new UserDTO(user_id.getValue(), data.getUsers().getData().get(user_id).username()));
        }
        return userDTOs;
    }

    private record UserDTO(Long user_id, String username){ }
}
