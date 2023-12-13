package org.apostolis.common.PersistenseDataTypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apostolis.comments.domain.CommentDetails;
import org.apostolis.comments.domain.CommentId;
import org.apostolis.posts.domain.PostId;

import java.util.Map;

@AllArgsConstructor
@Getter
public class CommentsByPostId {
    private Map<PostId, Map<CommentId, CommentDetails>> data;
}
