package org.apostolis.posts.application.ports.in;

import org.apostolis.posts.domain.FollowingPostsView;
import org.apostolis.posts.domain.PostsWithNLatestCommentsView;

public interface PostViewsUseCase {
    FollowingPostsView getFollowingPosts(PostViewsQuery viewQuery) throws Exception;
    PostsWithNLatestCommentsView getOwnPostsWithNLatestComments(OwnPostsWithNCommentsQuery viewQuery) throws Exception;
    PostsWithNLatestCommentsView getPostWithNLatestComments(PostWithNCommentsQuery viewQuery) throws Exception;

}
