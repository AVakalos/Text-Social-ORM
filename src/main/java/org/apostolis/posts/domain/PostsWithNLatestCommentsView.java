package org.apostolis.posts.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apostolis.common.PersistenseDataTypes.CommentsByPostId;
import org.apostolis.common.PersistenseDataTypes.PostsById;

@AllArgsConstructor
@Getter
public class PostsWithNLatestCommentsView {
    private PostsById posts;
    private CommentsByPostId commentsPerPost;
}
