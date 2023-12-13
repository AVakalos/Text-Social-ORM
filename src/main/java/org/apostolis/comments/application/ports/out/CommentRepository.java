package org.apostolis.comments.application.ports.out;

import org.apostolis.comments.domain.Comment;
import org.apostolis.common.PersistenseDataTypes.CommentsByPostId;
import org.apostolis.common.PageRequest;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.UserId;

import java.util.List;

public interface CommentRepository {
    void saveComment(Comment commentToSave);
    long getCountOfUserCommentsUnderThisPost(UserId user, PostId post);
    CommentsByPostId getCommentsGivenPostIds(List<PostId> post_ids, PageRequest req);
    CommentsByPostId getLatestCommentsGivenPostIds(List<PostId> post_ids);
}
