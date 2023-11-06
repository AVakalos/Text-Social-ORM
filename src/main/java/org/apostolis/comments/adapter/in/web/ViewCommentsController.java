package org.apostolis.comments.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;

public class ViewCommentsController {

    private final CommentsViewsUseCase CommentsViewService;

    public ViewCommentsController(CommentsViewsUseCase commentsViewService) {
        CommentsViewService = commentsViewService;
    }

    public void getCommentsOnOwnPosts(Context ctx){
        int pageNum;
        int pageSize;
        try{
            pageNum = ctx.queryParamAsClass("page", Integer.class).get();
            pageSize = ctx.queryParamAsClass("size", Integer.class).get();
        }catch (Exception e){
            throw new IllegalArgumentException("page and size must be positive integers");
        }
    }

    public void getLatestCommentsOnOwnOrFollowingPosts(Context ctx){
        int pageNum;
        int pageSize;
        try{
            pageNum = ctx.queryParamAsClass("page", Integer.class).get();
            pageSize = ctx.queryParamAsClass("size", Integer.class).get();
        }catch (Exception e){
            throw new IllegalArgumentException("page and size must be positive integers");
        }
    }
}
