package org.apostolis.common.PersistenseDataTypes;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apostolis.posts.domain.PostDetails;
import org.apostolis.posts.domain.PostId;

import java.util.Map;

@AllArgsConstructor
@Getter
public class PostsById {
    private Map<PostId, PostDetails> data;
}
