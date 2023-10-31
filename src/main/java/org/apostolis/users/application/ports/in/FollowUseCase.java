package org.apostolis.users.application.ports.in;

public interface FollowUseCase {
    void followUser(FollowCommand followCommand) throws Exception;
}
