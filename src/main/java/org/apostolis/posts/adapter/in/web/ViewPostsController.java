package org.apostolis.posts.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.App;
import org.apostolis.posts.application.ports.in.PostViewsUseCase;
import org.apostolis.posts.application.ports.in.OwnPostsWithNCommentsQuery;
import org.apostolis.posts.application.ports.in.PostViewsQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewPostsController {
    private final PostViewsUseCase postViewsService;


    public ViewPostsController(PostViewsUseCase postViewsService) {
        this.postViewsService = postViewsService;
    }

    public void getFollowingPosts(Context ctx){
        int pageNum;
        int pageSize;
        try{
            pageNum = ctx.queryParamAsClass("page", Integer.class).get();
            pageSize = ctx.queryParamAsClass("size", Integer.class).get();
        }catch (Exception e){
            throw new IllegalArgumentException("page and size must be positive integers");
        }
        PostViewsQuery postViewsQuery = new PostViewsQuery(App.currentUserId.get(),pageNum,pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("data",postViewsService.getFollowingPosts(postViewsQuery));
        response.put("current_page",pageNum);
        response.put("page_size",pageSize);
        ctx.json(response);
    }

    public void getOwnPostsWithLatestNComments(Context ctx){
        int pageNum;
        int pageSize;
        int commentsNum;
        try{
            pageNum = ctx.queryParamAsClass("page", Integer.class).get();
            pageSize = ctx.queryParamAsClass("size", Integer.class).get();
            commentsNum = ctx.queryParamAsClass("comments", Integer.class).get();

        }catch (Exception e){
            throw new IllegalArgumentException("page and size and comments must be positive integers");
        }
        OwnPostsWithNCommentsQuery viewQuery = new OwnPostsWithNCommentsQuery(
                App.currentUserId.get(), commentsNum, pageNum, pageSize);

        Map<String, Object> response = new HashMap<>();
        Map<Integer,List<Object>> postsWithComments;
        postsWithComments = postViewsService.getOwnPostsWithNLatestComments(viewQuery);
        response.put("data",postsWithComments);
        response.put("current_page",pageNum);
        response.put("page_size",pageSize);
        ctx.json(response);
    }
}
