package org.apostolis.comments.application.ports.in;

import org.apostolis.posts.adapter.out.persistence.PostId;
import org.apostolis.users.adapter.out.persistence.UserId;
import java.util.List;
import java.util.Map;

public interface CommentsViewsUseCase {
    Map<PostId, List<Object>> getCommentsOnOwnPosts(ViewCommentsQuery viewCommentsQuery);
    Map<UserId, Map<PostId,List<Object>>> getLatestCommentsOnOwnOrFollowingPosts(ViewCommentsQuery viewCommentsQuery);
}
