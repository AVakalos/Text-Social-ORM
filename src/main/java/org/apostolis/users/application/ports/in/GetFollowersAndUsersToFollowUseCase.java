package org.apostolis.users.application.ports.in;

import java.util.ArrayList;

public interface GetFollowersAndUsersToFollowUseCase {
    ArrayList<String> getFollowers(int user);
    ArrayList<String> getUsersToFollow(int user);
}
