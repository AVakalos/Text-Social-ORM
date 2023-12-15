package org.apostolis.posts.application.ports.out;

import org.apostolis.common.PageRequest;
import org.apostolis.common.PersistenseDataTypes.CommentsByPostId;
import org.apostolis.common.PersistenseDataTypes.PostsById;
import org.apostolis.common.PersistenseDataTypes.PostsByUserId;
import org.apostolis.posts.domain.*;
import org.apostolis.users.domain.UserId;

import java.util.List;

public interface PostRepository {
    void savePost(Post postToSave);
    PostsById getPostById(PostId post_id);
    PostsByUserId getPostsGivenUsersIds(List<UserId> user_ids, PageRequest req);
    void registerLink(PostId post_id);
    boolean checkLink(PostId post_id);
    boolean isMyPost(UserId user_id, PostId post_id);
    long getCountOfUserCommentsUnderThisPost(UserId user, PostId post);
}
