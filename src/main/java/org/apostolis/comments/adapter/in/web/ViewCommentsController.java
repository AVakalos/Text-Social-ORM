package org.apostolis.comments.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.App;
import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.comments.application.ports.in.ViewCommentsQuery;
import org.apostolis.common.PageRequest;

import java.util.HashMap;
import java.util.Map;

public class ViewCommentsController {

    private final CommentsViewsUseCase commentsViewService;

    public ViewCommentsController(CommentsViewsUseCase commentsViewService) {
        this.commentsViewService = commentsViewService;
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
        ViewCommentsQuery viewsQuery = new ViewCommentsQuery(App.currentUserId.get(),new PageRequest(pageNum, pageSize));

        Map<String, Object> response = new HashMap<>();
        response.put("data",commentsViewService.getCommentsOnOwnPosts(viewsQuery));
        response.put("current_page",pageNum);
        response.put("page_size",pageSize);
        ctx.json(response);
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
        ViewCommentsQuery viewsQuery = new ViewCommentsQuery(App.currentUserId.get(),new PageRequest(pageNum, pageSize));
        Map<String, Object> response = new HashMap<>();
        response.put("data",commentsViewService.getLatestCommentsOnOwnOrFollowingPosts(viewsQuery));
        response.put("current_page",pageNum);
        response.put("page_size",pageSize);
        ctx.json(response);
    }
}
