package org.apostolis.comments.application.service;

import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.comments.application.ports.in.ViewCommentsQuery;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.common.PageRequest;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.users.application.ports.out.FollowsRepository;

import java.util.*;

public class CommentsViewService implements CommentsViewsUseCase {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final FollowsRepository followsRepository;

    public CommentsViewService(CommentRepository commentRepository, PostRepository postRepository, FollowsRepository followsRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.followsRepository = followsRepository;
    }

    @Override
    public Map<Long, List<Object>> getCommentsOnOwnPosts(ViewCommentsQuery viewCommentsQuery) {
        Map<Long, List<Object>> result = new LinkedHashMap<>();
        ArrayList<Long> user_id = new ArrayList<>(List.of(viewCommentsQuery.user()));
        HashMap<Long, String> ownPosts = postRepository.getPostsGivenUsersIds(
                user_id,new PageRequest(0, Integer.MAX_VALUE)).get(viewCommentsQuery.user());

        if(ownPosts == null){
            return result;
        }
        ArrayList<Long> post_ids = new ArrayList<>(ownPosts.keySet());
        HashMap<Long, HashMap<Long, String>> comments = commentRepository.getCommentsGivenPostIds(
                post_ids,viewCommentsQuery.pageRequest());

        for(long post_id: post_ids){
            if(comments.get(post_id) != null){
                List<Object> textAndComments = new ArrayList<>();
                textAndComments.add(ownPosts.get(post_id));
                textAndComments.add(comments.get(post_id));
                result.put(post_id,textAndComments);
            }
        }
        return result;
    }

    @Override
    public Map<Long, HashMap<Long,List<Object>>> getLatestCommentsOnOwnOrFollowingPosts(ViewCommentsQuery viewCommentsQuery) {

        // {user_id(me or users i follow):{post_id:[post_text,{comments_id:comment_text,...}],...},...}
        Map<Long, HashMap<Long,List<Object>>> result = new LinkedHashMap<>();
        HashMap<Long, String> following_users = followsRepository.getFollowing(viewCommentsQuery.user(),new PageRequest(0, Integer.MAX_VALUE));

        if(following_users.isEmpty()){
            return result;
        }

        ArrayList<Long> user_ids = new ArrayList<>(following_users.keySet());
        user_ids.add(viewCommentsQuery.user());

        HashMap<Long, HashMap<Long, String>> own_and_following_posts = postRepository.getPostsGivenUsersIds(
                user_ids, viewCommentsQuery.pageRequest());

        if(own_and_following_posts.isEmpty()){
            return result;
        }

        ArrayList<Long> post_ids = new ArrayList<>();
        for(long user: own_and_following_posts.keySet()){
            post_ids.addAll(own_and_following_posts.get(user).keySet());
        }

        HashMap<Long, HashMap<Long,String>> latest_comments = commentRepository.getCommentsGivenPostIds(
                post_ids, new PageRequest(0, 1));

        if(latest_comments.isEmpty()){
            return null;
        }

        for(long user: user_ids){
            // {post_id:[post_text,{comment_id: comment_text,...}],...}
            HashMap<Long, List<Object>> posts_and_latest_comment = new LinkedHashMap<>();

            HashMap<Long,String> user_posts = own_and_following_posts.get(user);
            if(user_posts!=null) {
                for (long post_id : user_posts.keySet()) {
                    List<Object> post_text_and_lastest_comment = new ArrayList<>();
                    post_text_and_lastest_comment.add(user_posts.get(post_id));

                    post_text_and_lastest_comment.add(
                            Objects.requireNonNullElse(latest_comments.get(post_id), "No comments"));
                    posts_and_latest_comment.put(post_id, post_text_and_lastest_comment);
                }
                result.put(user, posts_and_latest_comment);
            }
        }
        return result;
    }
}
