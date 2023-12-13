package org.apostolis.comments.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apostolis.common.PersistenseDataTypes.CommentsByPostId;
import org.apostolis.common.PersistenseDataTypes.PostsById;

@AllArgsConstructor
@Getter
public class CommentsOnOwnPostsView {
    private PostsById ownPosts;
    private CommentsByPostId commentsPerPost;
}
