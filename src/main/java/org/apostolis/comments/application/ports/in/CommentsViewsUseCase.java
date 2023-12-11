package org.apostolis.comments.application.ports.in;

import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.UserId;
import java.util.List;
import java.util.Map;

public interface CommentsViewsUseCase {
    Map<PostId, List<Object>> getCommentsOnOwnPosts(ViewCommentsQuery viewCommentsQuery) throws Exception;
    Map<UserId, Map<PostId,List<Object>>> getLatestCommentsOnOwnOrFollowingPosts(ViewCommentsQuery viewCommentsQuery) throws Exception;
}
