package org.apostolis.users.application.ports.in;

import org.apostolis.exception.DatabaseException;

public interface FollowsUseCase {
    void followUser(FollowsCommand followsCommand) throws Exception;
    void unfollowUser(FollowsCommand followsCommand) throws Exception;
}
