package org.apostolis.comments.application.ports.in;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public interface CommentsViewsUseCase {
    JSONObject getCommentsOnOwnPosts(int user_id, int pageNum, int pageSize);
    JSONObject getLatestCommentsOnOwnOrFollowingPosts(int user_id, int pageNum, int pageSize);
}
