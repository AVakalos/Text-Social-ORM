package org.apostolis.users.application.ports.in;

import java.util.ArrayList;
import java.util.HashMap;

public interface GetFollowersAndUsersToFollowUseCase {
    HashMap<Long, String> getFollowers(long userId);
    HashMap<Long, String> getUsersToFollow(long userId);
}
