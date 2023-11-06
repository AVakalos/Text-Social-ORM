package org.apostolis.posts.application.ports.in;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public interface PostViewsUseCase {
    JSONObject getFollowingPosts(PostViewsQuery viewQuery);

    JSONObject getOwnPostsWithNLatestComments(OwnPostsWithNCommentsQuery viewQuery);
}
