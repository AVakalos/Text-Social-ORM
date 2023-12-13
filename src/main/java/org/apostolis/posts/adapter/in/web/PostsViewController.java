package org.apostolis.posts.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.App;
import org.apostolis.comments.domain.CommentDetails;
import org.apostolis.comments.domain.CommentId;
import org.apostolis.common.PageRequest;
import org.apostolis.posts.application.ports.in.PostViewsUseCase;
import org.apostolis.posts.application.ports.in.OwnPostsWithNCommentsQuery;
import org.apostolis.posts.application.ports.in.PostViewsQuery;
import org.apostolis.posts.domain.FollowingPostsView;
import org.apostolis.posts.domain.PostDetails;
import org.apostolis.posts.domain.PostId;
import org.apostolis.posts.domain.PostsWithNLatestCommentsView;
import org.apostolis.users.domain.UserId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Handles the post views http requests
public class PostsViewController {
    private final PostViewsUseCase postViewsService;

    public PostsViewController(PostViewsUseCase postViewsService) {
        this.postViewsService = postViewsService;
    }

    public void getFollowingPosts(Context ctx) throws Exception {
        int pageNum;
        int pageSize;
        try{
            pageNum = ctx.queryParamAsClass("page", Integer.class).get();
            pageSize = ctx.queryParamAsClass("size", Integer.class).get();
        }catch (Exception e){
            throw new IllegalArgumentException("page and size must be positive integers");
        }
        PostViewsQuery postViewsQuery = new PostViewsQuery(App.currentUserId.get(), new PageRequest(pageNum,pageSize));

        FollowingPostsView data = postViewsService.getFollowingPosts(postViewsQuery);

        List<FollowingPostsViewDTO> followingPostsViewDTOs = new ArrayList<>();
        for(UserId following_user_id: data.getFollowingPosts().getData().keySet()){
            List<PostDTO> followingPostsDTOs = new ArrayList<>();
            Map<PostId, PostDetails> following_users_post = data.getFollowingPosts().getData().get(following_user_id);
            for(PostId post_id: following_users_post.keySet()){
                followingPostsDTOs.add(new PostDTO(post_id.getPost_id(), following_users_post.get(post_id).text()));
            }
            followingPostsViewDTOs.add(new FollowingPostsViewDTO(following_user_id.getUser_id(), followingPostsDTOs));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data",followingPostsViewDTOs);
        response.put("current_page",pageNum);
        response.put("page_size",pageSize);
        ctx.json(response);
    }

    public void getOwnPostsWithLatestNComments(Context ctx) throws Exception {
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
                App.currentUserId.get().getUser_id(), commentsNum, new PageRequest(pageNum,pageSize));

        PostsWithNLatestCommentsView data = postViewsService.getOwnPostsWithNLatestComments(viewQuery);
        List<PostWithCommentsDTO> postWithCommentsDTOS = new ArrayList<>();
        for(PostId post_id: data.getCommentsPerPost().getData().keySet()){
            long numericId = post_id.getPost_id();
            List<CommentDTO> commentDTOs = new ArrayList<>();
            Map<CommentId, CommentDetails> commentsOfPost = data.getCommentsPerPost().getData().get(post_id);
            for(CommentId comment_id: commentsOfPost.keySet()){
                commentDTOs.add(new CommentDTO(comment_id.getComment_id(), commentsOfPost.get(comment_id).text()));
            }
            postWithCommentsDTOS.add(
                    new PostWithCommentsDTO(numericId, data.getPosts().getData().get(post_id).text(),commentDTOs));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data",postWithCommentsDTOS);
        response.put("current_page",pageNum);
        response.put("page_size",pageSize);
        ctx.json(response);
    }

    private record PostWithCommentsDTO(Long post_id, String postText, List<CommentDTO> comments){ }

    private record CommentDTO(Long commentId, String commentText){ }

    private record FollowingPostsViewDTO(Long user_id, List<PostDTO> following_posts) {}

    private record PostDTO(Long post_id, String post_text){ }
}
