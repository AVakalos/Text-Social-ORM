package org.apostolis.posts.application.ports.out;

import org.apostolis.posts.domain.Post;

import java.util.ArrayList;
import java.util.HashMap;

public interface PostRepository {
    void savePost(Post postToSave);
    HashMap<Integer,HashMap<Integer,String>> getPostsGivenUsersIds(ArrayList<Integer> user_ids, int pageNum, int pageSize);
}
