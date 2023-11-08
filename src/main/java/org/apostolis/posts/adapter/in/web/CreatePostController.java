package org.apostolis.posts.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.App;
import org.apostolis.posts.application.ports.in.CreatePostCommand;
import org.apostolis.posts.application.ports.in.CreatePostUseCase;
import org.apostolis.posts.domain.CreatePostRequest;
import org.apostolis.security.TokenManager;

import java.util.Objects;

public class CreatePostController {
    private final CreatePostUseCase postService;

    private final TokenManager tokenManager;

    public CreatePostController(CreatePostUseCase postService, TokenManager tokenManager) {
        this.postService = postService;
        this.tokenManager = tokenManager;
    }

    public void createPost(Context ctx){
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String authlevel = tokenManager.extractRole(token);
        CreatePostRequest request = ctx.bodyAsClass(CreatePostRequest.class);
        CreatePostCommand createPostCommand = new CreatePostCommand(App.currentUserId.get(), request.text(), authlevel);
        postService.createPost(createPostCommand);
        ctx.result(tokenManager.extractUsername(token)+" did a new post.");
    }
}
