package org.apostolis.comments.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apostolis.common.PersistenseDataTypes.CommentsByPostId;
import org.apostolis.common.PersistenseDataTypes.PostsByUserId;

@AllArgsConstructor
@Getter
public class LatestCommentOnPostView {
    private PostsByUserId ownOrFollowingPosts;
    private CommentsByPostId latestCommentPerPost;
}
