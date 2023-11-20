package org.apostolis.comments.application.ports.out;

import org.apostolis.comments.domain.Comment;

import java.util.ArrayList;
import java.util.HashMap;

public interface CommentRepository {
    void saveComment(Comment commentToSave);
    long getCountOfUserCommentsUnderThisPost(long user, long post);
    HashMap<Long, HashMap<Long,String>> getCommentsGivenPostIds(ArrayList<Long> post_ids, int pageNum, int pageSize);
}
