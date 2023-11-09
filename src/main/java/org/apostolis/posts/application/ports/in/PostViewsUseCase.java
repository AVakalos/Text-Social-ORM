package org.apostolis.posts.application.ports.in;

import java.util.List;
import java.util.Map;

public interface PostViewsUseCase {
    Map<Integer, List<Object>> getFollowingPosts(PostViewsQuery viewQuery);
    Map<Integer,List<Object>> getOwnPostsWithNLatestComments(OwnPostsWithNCommentsQuery viewQuery);
}
