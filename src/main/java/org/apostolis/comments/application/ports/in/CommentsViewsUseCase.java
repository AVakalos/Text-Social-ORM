package org.apostolis.comments.application.ports.in;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface CommentsViewsUseCase {
    Map<Integer, List<Object>> getCommentsOnOwnPosts(ViewCommentsQuery viewCommentsQuery);
    Map<Integer, HashMap<Integer,List<Object>>> getLatestCommentsOnOwnOrFollowingPosts(ViewCommentsQuery viewCommentsQuery);
}
