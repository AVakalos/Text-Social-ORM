package org.apostolis.posts.application.ports.out;

import org.apostolis.posts.application.ports.in.CreateLinkCommand;
import org.apostolis.posts.domain.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public interface PostRepository {
    void savePost(Post postToSave);
    HashMap<Long, HashMap<Long,String>> getPostsGivenUsersIds(ArrayList<Long> user_ids, int pageNum, int pageSize);
    void registerLink(long post);
    boolean checkLink(long post);
    boolean isMyPost(long user, long post);
}
