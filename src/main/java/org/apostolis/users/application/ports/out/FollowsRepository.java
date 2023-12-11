package org.apostolis.users.application.ports.out;

import org.apostolis.common.PageRequest;
import org.apostolis.exception.DatabaseException;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.domain.UserDTO;
import java.util.Map;

public interface FollowsRepository {
    void saveFollow(UserId user, UserId userToFollow) throws DatabaseException;
    void deleteFollow(UserId user, UserId userToUnfollow) throws DatabaseException;

    Map<UserId, UserDTO> getFollowers(UserId user, PageRequest req) throws DatabaseException;
    Map<UserId, UserDTO> getFollowing(UserId user, PageRequest req) throws DatabaseException;
    Map<UserId, UserDTO> getUsersToFollow(UserId user, PageRequest req) throws DatabaseException;
}
