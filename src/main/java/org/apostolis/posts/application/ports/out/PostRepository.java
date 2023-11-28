package org.apostolis.posts.application.ports.out;

import org.apostolis.common.PageRequest;
import org.apostolis.posts.adapter.out.persistence.PostId;
import org.apostolis.posts.domain.Post;
import org.apostolis.posts.domain.PostDTO;
import org.apostolis.users.adapter.out.persistence.UserId;

import java.util.List;
import java.util.Map;

public interface PostRepository {
    void savePost(Post postToSave);
    Map<PostId,PostDTO> getPostById(PostId post_id);
    Map<UserId, Map<PostId, PostDTO>> getPostsGivenUsersIds(List<UserId> user_ids, PageRequest req);
    void registerLink(PostId post_id);
    boolean checkLink(PostId post_id);
    boolean isMyPost(UserId user_id, PostId post_id);
}
