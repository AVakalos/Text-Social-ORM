package org.apostolis.comments.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.comments.domain.CommentId;
import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.comments.application.ports.in.ViewCommentsQuery;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.CommentDTO;
import org.apostolis.common.PageRequest;
import org.apostolis.common.TransactionUtils;
import org.apostolis.posts.domain.PostId;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.PostDTO;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.domain.UserDTO;
import org.hibernate.Session;

import java.util.*;

// Business logic for comments views
@RequiredArgsConstructor
public class CommentsViewService implements CommentsViewsUseCase {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final FollowsRepository followsRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public Map<PostId, List<Object>> getCommentsOnOwnPosts(ViewCommentsQuery viewCommentsQuery) throws Exception {
        TransactionUtils.ThrowingFunction<Session, Map<PostId, List<Object>>, Exception> getComments = (session) -> {
            Map<PostId, List<Object>> result = new LinkedHashMap<>();
            ArrayList<UserId> user_id = new ArrayList<>(List.of(viewCommentsQuery.user_id()));
            Map<PostId, PostDTO> ownPosts = postRepository.getPostsGivenUsersIds(
                    user_id,new PageRequest(0, Integer.MAX_VALUE)).get(viewCommentsQuery.user_id());

            if(ownPosts == null){
                return result;
            }
            ArrayList<PostId> post_ids = new ArrayList<>(ownPosts.keySet());
            Map<PostId, Map<CommentId,CommentDTO>> comments = commentRepository.getCommentsGivenPostIds(
                    post_ids,viewCommentsQuery.pageRequest());

            for(PostId post_id: post_ids){
                if(comments.get(post_id) != null){
                    List<Object> textAndComments = new ArrayList<>();
                    textAndComments.add(ownPosts.get(post_id));
                    textAndComments.add(comments.get(post_id));
                    result.put(post_id,textAndComments);
                }
            }
            return result;
        };
        return transactionUtils.doInTransaction(getComments);
    }

    @Override
    public Map<UserId, Map<PostId,List<Object>>> getLatestCommentsOnOwnOrFollowingPosts(ViewCommentsQuery viewCommentsQuery) throws Exception {
        TransactionUtils.ThrowingFunction<Session, Map<UserId, Map<PostId,List<Object>>>, Exception> getLatestComments = (session) -> {
            // {user_id(me or users i follow):{post_id:[post_text,{comments_id:comment_text,...}],...},...}
            Map<UserId, Map<PostId,List<Object>>> result = new LinkedHashMap<>();
            Map<UserId, UserDTO> following_users = followsRepository.getFollowing(viewCommentsQuery.user_id(),new PageRequest(0, Integer.MAX_VALUE));

            ArrayList<UserId> user_ids = new ArrayList<>(following_users.keySet());
            user_ids.add(viewCommentsQuery.user_id());

            Map<UserId, Map<PostId, PostDTO>> own_and_following_posts = postRepository.getPostsGivenUsersIds(
                    user_ids, viewCommentsQuery.pageRequest());

            if(own_and_following_posts.isEmpty()){
                return result;
            }

            ArrayList<PostId> post_ids = new ArrayList<>();
            for(UserId user: own_and_following_posts.keySet()){
                post_ids.addAll(own_and_following_posts.get(user).keySet());
            }

            Map<PostId,Map<CommentId,CommentDTO>> latest_comments = commentRepository.getLatestCommentsGivenPostIds(post_ids);

            if(latest_comments.isEmpty()){
                return null;
            }

            for(UserId user: user_ids){
                // {post_id:[post_text,{comment_id: comment_text,...}],...}
                HashMap<PostId, List<Object>> posts_and_latest_comment = new LinkedHashMap<>();

                Map<PostId,PostDTO> user_posts = own_and_following_posts.get(user);
                if(user_posts!=null) {
                    for (PostId post_id : user_posts.keySet()) {
                        List<Object> post_text_and_latest_comment = new ArrayList<>();
                        post_text_and_latest_comment.add(user_posts.get(post_id));

                        post_text_and_latest_comment.add(
                                Objects.requireNonNullElse(latest_comments.get(post_id), "No comments"));
                        posts_and_latest_comment.put(post_id, post_text_and_latest_comment);
                    }
                    result.put(user, posts_and_latest_comment);
                }
            }
            return result;
        };
        return transactionUtils.doInTransaction(getLatestComments);
    }
}