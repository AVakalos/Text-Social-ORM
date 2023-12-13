package org.apostolis.users.application.ports.in;

public interface FollowsUseCase {
    void followUser(FollowsCommand followsCommand) throws Exception;
    void unfollowUser(FollowsCommand followsCommand) throws Exception;
}
