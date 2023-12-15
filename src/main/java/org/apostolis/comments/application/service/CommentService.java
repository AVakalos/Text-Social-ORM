package org.apostolis.comments.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.comments.adapter.out.persistence.CommentEntity;
import org.apostolis.comments.application.ports.in.CreateCommentCommand;
import org.apostolis.comments.application.ports.in.CreateCommentUseCase;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.common.TransactionUtils;
import org.apostolis.posts.adapter.out.persistence.PostEntity;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.Post;
import org.apostolis.users.domain.Role;
import org.hibernate.Session;

// Business logic for comment crud operations
@RequiredArgsConstructor
public class CommentService implements CreateCommentUseCase {

    private final PostRepository postRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public void createComment(CreateCommentCommand createCommentCommand) throws Exception {
        TransactionUtils.ThrowingConsumer<Session,Exception> createComment = (session) -> {
            PostEntity targetPostEntity = session.find(PostEntity.class, createCommentCommand.post().getPost_id());

            if(targetPostEntity == null){
                throw new CommentCreationException("Couldn't find the post with id: "+createCommentCommand.post().getPost_id());
            }
            Comment newComment = new Comment(createCommentCommand.user(), createCommentCommand.post(), createCommentCommand.text());
            Role role = Role.valueOf(createCommentCommand.role());
            long comments_count = 0;
            if (role == Role.FREE){
                comments_count = postRepository.getCountOfUserCommentsUnderThisPost(createCommentCommand.user(),createCommentCommand.post());
            }
            Post targetPost = targetPostEntity.mapToDomain();
            targetPost.addComment(newComment,comments_count,role);

            targetPostEntity.addComment(CommentEntity.mapToEntity(newComment,targetPostEntity));
            session.persist(targetPostEntity);
        };
        transactionUtils.doInTransaction(createComment);
    }
}
