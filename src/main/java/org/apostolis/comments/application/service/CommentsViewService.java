package org.apostolis.comments.application.service;

import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.comments.application.ports.in.ViewCommentsQuery;
import org.apostolis.comments.application.ports.out.CommentRepository;
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
    public Map<Integer, List<Object>> getCommentsOnOwnPosts(ViewCommentsQuery viewCommentsQuery) {
        Map<Integer, List<Object>> result = new LinkedHashMap<>();
        ArrayList<Integer> user_id = new ArrayList<>(List.of(viewCommentsQuery.user()));
        HashMap<Integer,String> ownPosts = postRepository.getPostsGivenUsersIds(
                user_id,0, Integer.MAX_VALUE).get(viewCommentsQuery.user());

        if(ownPosts == null){
            return result;
        }
        ArrayList<Integer> post_ids = new ArrayList<>(ownPosts.keySet());
        HashMap<Integer, HashMap<Integer, String>> comments = commentRepository.getCommentsGivenPostIds(
                post_ids,viewCommentsQuery.pageNum(),viewCommentsQuery.pageSize());

        for(int post_id: post_ids){
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
    public Map<Integer, HashMap<Integer,List<Object>>> getLatestCommentsOnOwnOrFollowingPosts(ViewCommentsQuery viewCommentsQuery) {

        // {user_id(me or users i follow):{post_id:[post_text,{comments_id:comment_text,...}],...},...}
        Map<Integer, HashMap<Integer,List<Object>>> result = new LinkedHashMap<>();
        HashMap<Integer, String> following_users = followsRepository.getFollowing(viewCommentsQuery.user());

        if(following_users.isEmpty()){
            return result;
        }

        ArrayList<Integer> user_ids = new ArrayList<>(following_users.keySet());
        user_ids.add(viewCommentsQuery.user());

        HashMap<Integer, HashMap<Integer, String>> own_and_following_posts = postRepository.getPostsGivenUsersIds(
                user_ids, viewCommentsQuery.pageNum(), viewCommentsQuery.pageSize());

        if(own_and_following_posts.isEmpty()){
            return result;
        }

        ArrayList<Integer> post_ids = new ArrayList<>();
        for(int user: own_and_following_posts.keySet()){
            post_ids.addAll(own_and_following_posts.get(user).keySet());
        }

        HashMap<Integer, HashMap<Integer,String>> latest_comments = commentRepository.getCommentsGivenPostIds(
                post_ids, 0, 1);

        if(latest_comments.isEmpty()){
            return null;
        }

        for(int user: user_ids){
            // {post_id:[post_text,{comment_id: comment_text,...}],...}
            HashMap<Integer, List<Object>> posts_and_latest_comment = new LinkedHashMap<>();

            HashMap<Integer,String> user_posts = own_and_following_posts.get(user);
            if(user_posts!=null) {
                for (int post_id : user_posts.keySet()) {
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
