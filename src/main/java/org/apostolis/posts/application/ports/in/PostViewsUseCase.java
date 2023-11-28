package org.apostolis.posts.application.ports.in;

import org.apostolis.posts.adapter.out.persistence.PostId;
import org.apostolis.users.adapter.out.persistence.UserId;

import java.util.List;
import java.util.Map;

public interface PostViewsUseCase {
    Map<UserId, List<Object>> getFollowingPosts(PostViewsQuery viewQuery);
    Map<PostId,List<Object>> getOwnPostsWithNLatestComments(OwnPostsWithNCommentsQuery viewQuery);
    List<Object> getPostWithNLatestComments(PostWithNCommentsQuery viewQuery);

}
