package org.apostolis.comments.adapter.in.web;

import io.javalin.http.Context;
import org.apostolis.App;
import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.comments.application.ports.in.ViewCommentsQuery;
import org.apostolis.comments.domain.CommentDetails;
import org.apostolis.comments.domain.CommentId;
import org.apostolis.comments.domain.CommentsOnOwnPostsView;
import org.apostolis.comments.domain.LatestCommentOnPostView;
import org.apostolis.common.PageRequest;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.UserId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// handling data retrieval about comments http requests
public class CommentsViewController {

    private final CommentsViewsUseCase commentsViewService;

    public CommentsViewController(CommentsViewsUseCase commentsViewService) {
        this.commentsViewService = commentsViewService;
    }

    public void getCommentsOnOwnPosts(Context ctx) throws Exception {
        ViewCommentsQuery viewsQuery = createQueryFromRequest(ctx);
        CommentsOnOwnPostsView requestData = commentsViewService.getCommentsOnOwnPosts(viewsQuery);

        // Map View to DTO
        List<PostWithCommentsDTO> postWithCommentsDTOS = new ArrayList<>();
        for(PostId post_id: requestData.getCommentsPerPost().getData().keySet()){

            long numericId = post_id.getPost_id();
            Map<CommentId, CommentDetails> postComments = requestData.getCommentsPerPost().getData().get(post_id);
            List<CommentDTO> commentDTOs = new ArrayList<>();

            for(CommentId commentid: postComments.keySet()){
                commentDTOs.add(new CommentDTO(commentid.getComment_id(), postComments.get(commentid).text()));
            }
            postWithCommentsDTOS.add(new PostWithCommentsDTO(
                    numericId,requestData.getOwnPosts().getData().get(post_id).text(),commentDTOs));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data",postWithCommentsDTOS);
        response.put("current_page",viewsQuery.pageRequest().pageNumber());
        response.put("page_size",viewsQuery.pageRequest().pageSize());
        ctx.json(response);
    }

    public void getLatestCommentsOnOwnOrFollowingPosts(Context ctx) throws Exception {
        ViewCommentsQuery viewsQuery = createQueryFromRequest(ctx);

        LatestCommentOnPostView requestData = commentsViewService.getLatestCommentsOnOwnOrFollowingPosts(viewsQuery);

        List<UserWithPostsAndLatestComment> user_with_posts_and_latest_comments = new ArrayList<>();

        for(UserId user_id: requestData.getOwnOrFollowingPosts().getData().keySet()){

            List<PostWithLatestCommentDTO> posts_with_latest_comments = new ArrayList<>();
            for(PostId post_id: requestData.getOwnOrFollowingPosts().getData().get(user_id).keySet()) {
                // Not all posts have comments
                if(!requestData.getLatestCommentPerPost().getData().containsKey(post_id)){ continue; }

                ArrayList<CommentId> latestCommentIdList =
                        new ArrayList<>(requestData.getLatestCommentPerPost().getData().get(post_id).keySet());

                CommentId latestCommentId= latestCommentIdList.get(0);
                CommentDetails commentDetails =
                        requestData.getLatestCommentPerPost().getData().get(post_id).get(latestCommentId);

                posts_with_latest_comments.add(
                        new PostWithLatestCommentDTO(post_id.getPost_id(),
                                requestData.getOwnOrFollowingPosts().getData().get(user_id).get(post_id).text(),
                                latestCommentId.getComment_id(),
                                commentDetails.text())
                );
            }
            user_with_posts_and_latest_comments.add(
                    new UserWithPostsAndLatestComment(user_id.getUser_id(), posts_with_latest_comments));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data",user_with_posts_and_latest_comments);
        response.put("current_page",viewsQuery.pageRequest().pageNumber());
        response.put("page_size",viewsQuery.pageRequest().pageSize());
        ctx.json(response);
    }

    private ViewCommentsQuery createQueryFromRequest(Context ctx){
        int pageNum;
        int pageSize;
        try{
            pageNum = ctx.queryParamAsClass("page", Integer.class).get();
            pageSize = ctx.queryParamAsClass("size", Integer.class).get();
        }catch (Exception e){
            throw new IllegalArgumentException("page and size must be positive integers");
        }
        return new ViewCommentsQuery(App.currentUserId.get(),new PageRequest(pageNum, pageSize));
    }

    private record UserWithPostsAndLatestComment(Long user_id, List<PostWithLatestCommentDTO> posts_with_latest_comment){ }

    private record PostWithLatestCommentDTO(Long post_id, String post_text, Long comment_id, String comment_text) { }

    private record PostWithCommentsDTO(Long post_id, String postText, List<CommentDTO> comments){ }

    private record CommentDTO(Long commentId, String commentText){ }
}
