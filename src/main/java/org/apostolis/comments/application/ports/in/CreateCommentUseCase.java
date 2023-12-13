package org.apostolis.comments.application.ports.in;

public interface CreateCommentUseCase {
    void createComment(CreateCommentCommand createCommentCommand) throws Exception;
}
