package org.apostolis.comments.application.ports.in;

import org.apostolis.comments.domain.CommentsOnOwnPostsView;
import org.apostolis.comments.domain.LatestCommentOnPostView;

public interface CommentsViewsUseCase {
    CommentsOnOwnPostsView getCommentsOnOwnPosts(ViewCommentsQuery viewCommentsQuery) throws Exception;
    LatestCommentOnPostView getLatestCommentsOnOwnOrFollowingPosts(ViewCommentsQuery viewCommentsQuery) throws Exception;
}
