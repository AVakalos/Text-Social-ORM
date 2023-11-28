package org.apostolis.users.application.ports.in;

import org.apostolis.users.adapter.out.persistence.UserId;
import org.apostolis.users.domain.UserDTO;
import java.util.Map;

public interface GetFollowersAndUsersToFollowUseCase {
    Map<UserId, UserDTO> getFollowers(UserId userId, int pageNum, int pageSize);
    Map<UserId, UserDTO> getUsersToFollow(UserId userId, int pageNum, int pageSize);
}
