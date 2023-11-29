package org.apostolis.posts.application.service;

import org.apostolis.comments.adapter.out.persistence.CommentId;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.CommentDTO;
import org.apostolis.common.PageRequest;
import org.apostolis.posts.adapter.out.persistence.PostId;
import org.apostolis.posts.application.ports.in.PostViewsUseCase;
import org.apostolis.posts.application.ports.in.PostWithNCommentsQuery;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.application.ports.in.OwnPostsWithNCommentsQuery;
import org.apostolis.posts.application.ports.in.PostViewsQuery;
import org.apostolis.posts.domain.PostDTO;
import org.apostolis.users.adapter.out.persistence.UserId;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.domain.UserDTO;

import java.util.*;
import java.util.List;

// Post views business logic
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
    public Map<UserId,List<Object>> getFollowingPosts(PostViewsQuery postViewsQuery) {

        // {following_person1_id: [following_person_name,{post1_id: post1_text,...}],...}
        Map<UserId,List<Object>> result = new LinkedHashMap<>();

        Map<UserId, UserDTO> following_users = followsRepository.getFollowing(postViewsQuery.user(),postViewsQuery.pageRequest());
        if(following_users.isEmpty()){
            return result;
        }
        ArrayList<UserId> listOfFollowingIds = new ArrayList<>(following_users.keySet());

        Map<UserId, Map<PostId, PostDTO>> following_posts =
                postRepository.getPostsGivenUsersIds(listOfFollowingIds, new PageRequest(0, Integer.MAX_VALUE));

        if(following_posts.isEmpty()){
            return result;
        }

        for(UserId following_id: listOfFollowingIds){
            List<Object> nameAndPosts = new ArrayList<>();
            nameAndPosts.add(following_users.get(following_id));
            nameAndPosts.add(Objects.requireNonNullElse(following_posts.get(following_id), "No posts"));
            result.put(following_id, nameAndPosts);
        }
        return result;
    }

    @Override
    public Map<PostId,List<Object>> getOwnPostsWithNLatestComments(OwnPostsWithNCommentsQuery viewQuery) {
        UserId user = new UserId(viewQuery.user());

        // {own_post_id:[post_text,{comments_id: comment,...}],...}
        Map<PostId,List<Object>> result = new LinkedHashMap<>();

        ArrayList<UserId> user_id = new ArrayList<>(List.of(user));
        Map<UserId, Map<PostId, PostDTO>> posts =
                postRepository.getPostsGivenUsersIds(user_id,viewQuery.pageRequest());
        if(posts.isEmpty()){
            return result;
        }
        Map<PostId,PostDTO> ownPosts = posts.get(user);

        ArrayList<PostId> listOfPostIds = new ArrayList<>(ownPosts.keySet());
        Map<PostId,Map<CommentId, CommentDTO>> latest_N_comments =
                commentRepository.getCommentsGivenPostIds(listOfPostIds,viewQuery.pageRequest());

        for(PostId post_id: ownPosts.keySet()){
            List<Object> postTextAndComments = new ArrayList<>();
            postTextAndComments.add(ownPosts.get(post_id));

            Map<CommentId, CommentDTO> commentsMap = latest_N_comments.get(post_id);
            postTextAndComments.add(Objects.requireNonNullElse(commentsMap, "No comments"));
            result.put(post_id,postTextAndComments);
        }
        return result;
    }

    @Override
    public List<Object> getPostWithNLatestComments(PostWithNCommentsQuery viewQuery) {
        Map<PostId, PostDTO> post = postRepository.getPostById(new PostId(viewQuery.post_id()));
        ArrayList<PostId> id = new ArrayList<>(post.keySet());
        Map<PostId,Map<CommentId,CommentDTO>> latest_N_comments =
                commentRepository.getCommentsGivenPostIds(id,new PageRequest(0, viewQuery.comments_num()));

        List<Object> postTextAndComments = new ArrayList<>();
        postTextAndComments.add(post.get(id.get(0)).text());
        postTextAndComments.add(latest_N_comments.get(id.get(0)));

        return postTextAndComments;
    }
}
