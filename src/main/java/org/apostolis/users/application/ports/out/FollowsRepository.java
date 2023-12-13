package org.apostolis.users.application.ports.out;

import org.apostolis.common.PageRequest;
import org.apostolis.common.PersistenseDataTypes.UsersById;
import org.apostolis.exception.DatabaseException;
import org.apostolis.users.domain.UserId;

public interface FollowsRepository {
    void saveFollow(UserId user, UserId userToFollow) throws DatabaseException;
    void deleteFollow(UserId user, UserId userToUnfollow) throws DatabaseException;
    UsersById getFollowers(UserId user, PageRequest req) throws DatabaseException;
    UsersById getFollowing(UserId user, PageRequest req) throws DatabaseException;
    UsersById getUsersToFollow(UserId user, PageRequest req) throws DatabaseException;
}
