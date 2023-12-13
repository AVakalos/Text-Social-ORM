package org.apostolis.comments.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.AppConfig;
import org.apostolis.comments.application.ports.in.CreateCommentCommand;
import org.apostolis.comments.application.ports.in.CreateCommentUseCase;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.common.TransactionUtils;
import org.apostolis.users.domain.Role;
import org.hibernate.Session;

// Business logic for comment creation
@RequiredArgsConstructor
public class CommentService implements CreateCommentUseCase {

    private final CommentRepository commentRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public void createComment(CreateCommentCommand createCommentCommand) throws Exception {
        TransactionUtils.ThrowingConsumer<Session,Exception> createComment = (session) -> {
            Role role = Role.valueOf(createCommentCommand.role());
            if(role.equals(Role.FREE)){
                long comments_count = commentRepository.getCountOfUserCommentsUnderThisPost(
                        createCommentCommand.user(), createCommentCommand.post());

                int max_comments_number = AppConfig.getFREE_MAX_COMMENTS();
                if(comments_count >= max_comments_number){
                    throw new CommentCreationException("Free users can comment up to "+ max_comments_number +" times per post."+
                            "\nYou reached the maximum number of comments for this post.");
                }
            }
            Comment commentToSave = new Comment(createCommentCommand.user(), createCommentCommand.post(), createCommentCommand.text());
            commentRepository.saveComment(commentToSave);
        };
        transactionUtils.doInTransaction(createComment);
    }
}
