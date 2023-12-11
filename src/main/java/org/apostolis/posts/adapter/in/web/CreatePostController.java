package org.apostolis.posts.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.App;
import org.apostolis.posts.application.ports.in.CreatePostCommand;
import org.apostolis.posts.application.ports.in.CreatePostUseCase;
import org.apostolis.posts.domain.CreatePostRequest;
import org.apostolis.security.TokenManager;

import java.util.Objects;

// Handles the post creation http requests
public class CreatePostController {
    private final CreatePostUseCase postService;

    private final TokenManager tokenManager;

    public CreatePostController(CreatePostUseCase postService, TokenManager tokenManager) {
        this.postService = postService;
        this.tokenManager = tokenManager;
    }

    public void createPost(Context ctx) throws Exception {
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String authenticationLevel = tokenManager.extractRole(token);
        CreatePostRequest request = ctx.bodyAsClass(CreatePostRequest.class);
        CreatePostCommand createPostCommand = new CreatePostCommand(
                App.currentUserId.get(), request.text(), authenticationLevel);
        postService.createPost(createPostCommand);
        ctx.result(tokenManager.extractUsername(token)+" did a new post.");
    }
}
