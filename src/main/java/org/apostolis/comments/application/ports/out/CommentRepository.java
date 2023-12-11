package org.apostolis.comments.application.ports.out;

import org.apostolis.comments.domain.CommentId;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentDTO;
import org.apostolis.common.PageRequest;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.UserId;

import java.util.List;
import java.util.Map;

public interface CommentRepository {
    void saveComment(Comment commentToSave);
    long getCountOfUserCommentsUnderThisPost(UserId user, PostId post);
    Map<PostId,Map<CommentId,CommentDTO>> getCommentsGivenPostIds(List<PostId> post_ids, PageRequest req);
    Map<PostId, Map<CommentId, CommentDTO>> getLatestCommentsGivenPostIds(List<PostId> post_ids);
}
