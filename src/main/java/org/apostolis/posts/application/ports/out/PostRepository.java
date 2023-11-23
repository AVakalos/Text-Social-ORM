package org.apostolis.posts.application.ports.out;

import org.apostolis.common.PageRequest;
import org.apostolis.posts.application.ports.in.CreateLinkCommand;
import org.apostolis.posts.domain.Post;
import org.apostolis.posts.domain.PostInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface PostRepository {
    void savePost(Post postToSave);
    PostInfo getPostById(long post);
    HashMap<Long, HashMap<Long, String>> getPostsGivenUsersIds(List<Long> user_ids, PageRequest req);
    void registerLink(long post);
    boolean checkLink(long post);
    boolean isMyPost(long user, long post);
}
