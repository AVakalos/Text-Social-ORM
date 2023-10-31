package org.apostolis.users.adapter.in.web;

import io.javalin.http.BadRequestResponse;
import org.apostolis.security.TokenManager;
import org.apostolis.users.application.ports.in.FollowCommand;
import org.apostolis.users.application.ports.in.FollowUseCase;

import io.javalin.http.Context;
import java.util.Objects;

public class FollowingController {
    private final FollowUseCase followUseCase;

    private final TokenManager tokenManager;

    public FollowingController(FollowUseCase followUseCase, TokenManager tokenManager) {
        this.followUseCase = followUseCase;
        this.tokenManager = tokenManager;
    }

    public void follow(Context ctx){
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String user = tokenManager.extractUsername(token);
        int follows = ctx.queryParamAsClass("follows", Integer.class).get();
        try{
            FollowCommand followCommand = new FollowCommand(user, follows);
            followUseCase.followUser(followCommand);
            ctx.result("User: "+user+
                    " followed user: "+follows);
        } catch (Exception e) {
            throw new BadRequestResponse(e.getMessage());
        }
    }

    public void unfollow(Context ctx){

    }
}
