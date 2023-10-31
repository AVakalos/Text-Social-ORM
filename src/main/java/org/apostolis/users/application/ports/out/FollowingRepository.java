package org.apostolis.users.application.ports.out;

import org.apostolis.users.application.ports.in.FollowCommand;

public interface FollowingRepository {
    void saveFollow(FollowCommand followCommand) throws Exception;
    //void deleteFollow(UnfollowCommand unfollowCommand);
}
