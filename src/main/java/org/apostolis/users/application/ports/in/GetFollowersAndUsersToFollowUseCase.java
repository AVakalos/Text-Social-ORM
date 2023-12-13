package org.apostolis.users.application.ports.in;

import org.apostolis.users.domain.FollowsView;
import org.apostolis.users.domain.UserId;

public interface GetFollowersAndUsersToFollowUseCase {
    FollowsView getFollowers(UserId userId, int pageNum, int pageSize) throws Exception;
    FollowsView getUsersToFollow(UserId userId, int pageNum, int pageSize) throws Exception;
}
