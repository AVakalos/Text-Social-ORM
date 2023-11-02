package org.apostolis.posts.application.ports.out;

import org.apostolis.posts.domain.Post;

public interface PostRepository {
    void savePost(Post postToSave);
}
