package org.apostolis.comments.application.service;

import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.json.JSONObject;

public class CommentsViewService implements CommentsViewsUseCase {

    private final PostRepository postRepository;

    private final FollowsRepository followsRepository;

    public CommentsViewService(PostRepository postRepository, FollowsRepository followsRepository) {
        this.postRepository = postRepository;
        this.followsRepository = followsRepository;
    }

    @Override
    public JSONObject getCommentsOnOwnPosts(int user_id, int pageNum, int pageSize) {
        return null;
    }

    @Override
    public JSONObject getLatestCommentsOnOwnOrFollowingPosts(int user_id, int pageNum, int pageSize) {
        return null;
    }
}
