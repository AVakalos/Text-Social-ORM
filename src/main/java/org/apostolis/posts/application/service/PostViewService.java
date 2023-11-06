package org.apostolis.posts.application.service;

import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.posts.application.ports.in.PostViewsUseCase;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.application.ports.in.OwnPostsWithNCommentsQuery;
import org.apostolis.posts.application.ports.in.PostViewsQuery;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    public JSONObject getFollowingPosts(PostViewsQuery postViewsQuery) {
        int user = postViewsQuery.user();
        int pageNum = postViewsQuery.pageNum();
        int pageSize = postViewsQuery.pageSize();

        HashMap<Integer,String> following = followsRepository.getFollowing(user);
        ArrayList<Integer> listOfFollowingIds = new ArrayList<>(following.keySet());

        HashMap<Integer, HashMap<Integer, String>> following_posts =
                postRepository.getPostsGivenUsersIds(listOfFollowingIds,pageNum,pageSize);

        HashMap<Integer, ArrayList<Object>> result = new LinkedHashMap<>();
        for(int key: listOfFollowingIds){
            //result.put(key,following_posts.get(key));
        }
        return null;
    }

    @Override
    public JSONObject getOwnPostsWithNLatestComments(OwnPostsWithNCommentsQuery viewQuery){
        int user = viewQuery.user();
        int pageNum = viewQuery.pageNum();
        int pageSize = viewQuery.pageSize();
        int commentsNum = viewQuery.commentsNum();

        ArrayList<Integer> user_id = new ArrayList<>(List.of(user));
        HashMap<Integer,String> ownPosts = postRepository.getPostsGivenUsersIds(user_id,pageNum,pageSize).get(user);

        ArrayList<Integer> listOfPostIds = new ArrayList<>(ownPosts.keySet());
        HashMap<Integer, HashMap<Integer,String>> comments =
                commentRepository.getCommentsGivenPostIds(listOfPostIds,0,commentsNum);

        HashMap<String, HashMap<Integer, String>> result = new LinkedHashMap<>();
        for(int key: listOfPostIds){
            result.put(ownPosts.get(key),comments.get(key));
        }
        return null;
    }
}
