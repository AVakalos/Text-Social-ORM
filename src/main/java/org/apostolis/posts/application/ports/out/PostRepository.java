package org.apostolis.posts.application.ports.out;

import org.apostolis.posts.application.ports.in.CreateLinkCommand;
import org.apostolis.posts.domain.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public interface PostRepository {
    void savePost(Post postToSave);
    HashMap<Integer,HashMap<Integer,String>> getPostsGivenUsersIds(ArrayList<Integer> user_ids, int pageNum, int pageSize);
    void registerLink(int post);
    boolean checkLink(int user, int post);
}
