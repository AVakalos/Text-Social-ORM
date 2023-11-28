package org.apostolis.comments.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.App;
import org.apostolis.comments.application.ports.in.CreateCommentCommand;
import org.apostolis.comments.application.ports.in.CreateCommentUseCase;
import org.apostolis.comments.domain.CreateCommentRequest;
import org.apostolis.security.TokenManager;

import java.util.Objects;

public class CreateCommentController {
    private final CreateCommentUseCase commentService;

    private final TokenManager tokenManager;


    public CreateCommentController(CreateCommentUseCase commentService, TokenManager tokenManager) {
        this.commentService = commentService;
        this.tokenManager = tokenManager;
    }

    public void createComment(Context ctx){
        String token = Objects.requireNonNull(ctx.header("Authorization")).substring(7);
        String authenticationLevel = tokenManager.extractRole(token);
        CreateCommentRequest request = ctx.bodyAsClass(CreateCommentRequest.class);
        CreateCommentCommand createCommentCommand = new CreateCommentCommand(
                App.currentUserId.get().getUser_id(), request.post(), request.text(), authenticationLevel);
        commentService.createComment(createCommentCommand);
        ctx.result(tokenManager.extractUsername(token)+" commented on post "+request.post()+".");
    }
}
