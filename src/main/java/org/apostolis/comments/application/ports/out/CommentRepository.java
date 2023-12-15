package org.apostolis.comments.application.ports.out;

import org.apostolis.common.PersistenseDataTypes.CommentsByPostId;
import org.apostolis.common.PageRequest;
import org.apostolis.posts.domain.PostId;

import java.util.List;

public interface CommentRepository {
    CommentsByPostId getCommentsGivenPostIds(List<PostId> post_ids, PageRequest req);
    CommentsByPostId getLatestCommentsGivenPostIds(List<PostId> post_ids);
}
