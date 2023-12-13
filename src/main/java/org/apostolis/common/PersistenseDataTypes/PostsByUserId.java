package org.apostolis.common.PersistenseDataTypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apostolis.posts.domain.PostDetails;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.UserId;

import java.util.Map;

@AllArgsConstructor
@Getter
public class PostsByUserId {
    private Map<UserId, Map<PostId, PostDetails>> data;
}
