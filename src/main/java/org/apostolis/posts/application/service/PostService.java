package org.apostolis.posts.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.common.TransactionUtils;
import org.apostolis.posts.application.ports.in.CreatePostCommand;
import org.apostolis.posts.application.ports.in.CreatePostUseCase;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.Post;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.domain.Role;
import org.hibernate.Session;

// Post creation business logic
@RequiredArgsConstructor
public class PostService implements CreatePostUseCase {

    private final PostRepository postRepository;

    private final TransactionUtils transactionUtils;

    @Override
    public void createPost(CreatePostCommand createPostCommand) throws Exception {
        TransactionUtils.ThrowingConsumer<Session,Exception> create = (session) -> {
            UserId user = createPostCommand.user();
            String text = createPostCommand.text();
            Role role = Role.valueOf(createPostCommand.role());

            Post postToSave = Post.createPost(user, text, role);
            postRepository.savePost(postToSave);
        };
        transactionUtils.doInTransaction(create);
    }
}
