package org.apostolis.comments.application.ports.out;

import org.apostolis.comments.domain.Comment;

public interface CommentRepository {
    void saveComment(Comment commentToSave);
    int getCountOfUserCommentsUnderThisPost(int user, int post);
}
