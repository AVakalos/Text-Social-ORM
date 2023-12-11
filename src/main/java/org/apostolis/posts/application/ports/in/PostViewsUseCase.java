package org.apostolis.posts.application.ports.in;

import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.UserId;

import java.util.List;
import java.util.Map;

public interface PostViewsUseCase {
    Map<UserId, List<Object>> getFollowingPosts(PostViewsQuery viewQuery) throws Exception;
    Map<PostId,List<Object>> getOwnPostsWithNLatestComments(OwnPostsWithNCommentsQuery viewQuery) throws Exception;
    List<Object> getPostWithNLatestComments(PostWithNCommentsQuery viewQuery) throws Exception;

}
