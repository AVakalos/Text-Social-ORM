package org.apostolis.comments.application.ports.in;

import org.apostolis.comments.domain.CommentCreationException;

public interface CreateCommentUseCase {
    void createComment(CreateCommentCommand createCommentCommand) throws CommentCreationException;
}
