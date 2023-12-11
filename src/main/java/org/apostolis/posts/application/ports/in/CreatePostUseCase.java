package org.apostolis.posts.application.ports.in;

public interface CreatePostUseCase {
    void createPost(CreatePostCommand createPostCommand) throws Exception;
}
