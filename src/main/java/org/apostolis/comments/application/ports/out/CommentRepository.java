package org.apostolis.comments.application.ports.out;

import org.apostolis.comments.domain.Comment;

import java.util.ArrayList;
import java.util.HashMap;

public interface CommentRepository {
    void saveComment(Comment commentToSave);
    int getCountOfUserCommentsUnderThisPost(int user, int post);
    HashMap<Integer, HashMap<Integer,String>> getCommentsGivenPostIds(ArrayList<Integer> post_ids, int pageNum, int pageSize);
    //HashMap<Integer, String> getLatestCommentsGivenPostIds(ArrayList<Integer> post_ids);
}
