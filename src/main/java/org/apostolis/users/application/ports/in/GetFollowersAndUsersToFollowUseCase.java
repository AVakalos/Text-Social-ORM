package org.apostolis.users.application.ports.in;

import java.util.ArrayList;
import java.util.HashMap;

public interface GetFollowersAndUsersToFollowUseCase {
    HashMap<Integer,String> getFollowers(int user);
    HashMap<Integer,String> getUsersToFollow(int user);
}
