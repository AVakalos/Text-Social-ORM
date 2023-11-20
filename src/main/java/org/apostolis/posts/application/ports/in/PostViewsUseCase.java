package org.apostolis.posts.application.ports.in;

import java.util.List;
import java.util.Map;

public interface PostViewsUseCase {
    Map<Long, List<Object>> getFollowingPosts(PostViewsQuery viewQuery);
    Map<Long,List<Object>> getOwnPostsWithNLatestComments(OwnPostsWithNCommentsQuery viewQuery);
}
