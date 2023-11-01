package org.apostolis.users.application.ports.in;

import org.apostolis.exception.DatabaseException;

public interface FollowsUseCase {
    void followUser(FollowsCommand followsCommand) throws DatabaseException, IllegalArgumentException;
    void unfollowUser(FollowsCommand followsCommand) throws DatabaseException, IllegalArgumentException;
}
