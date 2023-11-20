package org.apostolis.users.application.ports.out;

import org.apostolis.exception.DatabaseException;

import java.util.HashMap;

public interface FollowsRepository {
    void saveFollow(long user, long userToFollow) throws DatabaseException;
    void deleteFollow(long user, long userToUnfollow) throws DatabaseException;

    HashMap<Long, String> getFollowers(long user) throws DatabaseException;
    HashMap<Long, String> getFollowing(long user) throws DatabaseException;
    HashMap<Long, String> getUsersToFollow(long user) throws DatabaseException;
}
