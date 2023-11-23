package org.apostolis.users.application.ports.out;

import org.apostolis.common.PageRequest;
import org.apostolis.exception.DatabaseException;
import org.apostolis.users.domain.UserInfo;

import java.util.HashMap;
import java.util.List;

public interface FollowsRepository {
    void saveFollow(long user, long userToFollow) throws DatabaseException;
    void deleteFollow(long user, long userToUnfollow) throws DatabaseException;

    HashMap<Long, String> getFollowers(long user, PageRequest req) throws DatabaseException;
    HashMap<Long, String> getFollowing(long user, PageRequest req) throws DatabaseException;
    List<UserInfo> getUsersToFollow(long user, PageRequest req) throws DatabaseException;
}
