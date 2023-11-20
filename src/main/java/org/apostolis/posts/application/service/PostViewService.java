package org.apostolis.posts.application.service;

import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.posts.application.ports.in.PostViewsUseCase;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.application.ports.in.OwnPostsWithNCommentsQuery;
import org.apostolis.posts.application.ports.in.PostViewsQuery;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PostViewService implements PostViewsUseCase {
    private final PostRepository postRepository;

    private final FollowsRepository followsRepository;

    private final CommentRepository commentRepository;

    public PostViewService(PostRepository postRepository, FollowsRepository followsRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.followsRepository = followsRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public Map<Long,List<Object>> getFollowingPosts(PostViewsQuery postViewsQuery) {

        // {following_person1_id: [following_person_name,{post1_id: post1_text,...}],...}
        Map<Long,List<Object>> result = new LinkedHashMap<>();

        HashMap<Long, String> following_users = followsRepository.getFollowing(postViewsQuery.user());
        if(following_users.isEmpty()){
            return result;
        }
        ArrayList<Long> listOfFollowingIds = new ArrayList<>(following_users.keySet());

        HashMap<Long, HashMap<Long, String>> following_posts =
                postRepository.getPostsGivenUsersIds(listOfFollowingIds,postViewsQuery.pageNum(),postViewsQuery.pageSize());

        if(following_posts.isEmpty()){
            return result;
        }

        for(long following_id: listOfFollowingIds){
            List<Object> nameAndPosts = new ArrayList<>();
            nameAndPosts.add(following_users.get(following_id));
            nameAndPosts.add(Objects.requireNonNullElse(following_posts.get(following_id), "No posts"));
            result.put(following_id, nameAndPosts);
        }

        return result;
    }

    @Override
    public Map<Long,List<Object>> getOwnPostsWithNLatestComments(OwnPostsWithNCommentsQuery viewQuery) {
        long user = viewQuery.user();

        // {own_post_id:[post_text,{comments_id: comment,...}],...}
        Map<Long,List<Object>> result = new LinkedHashMap<>();

        ArrayList<Long> user_id = new ArrayList<>(List.of(user));
        HashMap<Long, HashMap<Long, String>> posts =
                postRepository.getPostsGivenUsersIds(user_id,viewQuery.pageNum(),viewQuery.pageSize());
        if(posts.isEmpty()){
            return result;
        }
        HashMap<Long,String> ownPosts = posts.get(user);

        ArrayList<Long> listOfPostIds = new ArrayList<>(ownPosts.keySet());
        HashMap<Long, HashMap<Long,String>> latest_N_comments =
                commentRepository.getCommentsGivenPostIds(listOfPostIds,0,viewQuery.commentsNum());


        for(long post_id: ownPosts.keySet()){

            List<Object> postTextAndComments = new ArrayList<>();
            postTextAndComments.add(ownPosts.get(post_id));

            HashMap<Long, String> commentsMap = latest_N_comments.get(post_id);
            postTextAndComments.add(Objects.requireNonNullElse(commentsMap, "No comments"));
            result.put(post_id,postTextAndComments);
        }
        return result;
    }
}
