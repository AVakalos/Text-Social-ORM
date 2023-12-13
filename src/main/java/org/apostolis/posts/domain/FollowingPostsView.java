package org.apostolis.posts.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apostolis.common.PersistenseDataTypes.PostsByUserId;

@AllArgsConstructor
@Getter
public class FollowingPostsView {
    PostsByUserId followingPosts;
}
