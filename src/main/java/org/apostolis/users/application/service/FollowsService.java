package org.apostolis.users.application.service;

import org.apostolis.exception.DatabaseException;
import org.apostolis.users.adapter.out.persistence.UserId;
import org.apostolis.users.application.ports.in.FollowsCommand;
import org.apostolis.users.application.ports.in.FollowsUseCase;
import org.apostolis.users.application.ports.out.FollowsRepository;

public class FollowsService implements FollowsUseCase {

    private final FollowsRepository followsRepository;

    public FollowsService(FollowsRepository followsRepository) {
        this.followsRepository = followsRepository;
    }

    @Override
    public void followUser(FollowsCommand followsCommand) throws DatabaseException, IllegalArgumentException{
        Long user = followsCommand.user_id();
        Long user_to_follow = followsCommand.follows();
        if (!user.equals(user_to_follow)){
            followsRepository.saveFollow(new UserId(user), new UserId(user_to_follow));
        }else{
            throw new IllegalArgumentException("You can't follow yourself");
        }
    }

    @Override
    public void unfollowUser(FollowsCommand followsCommand) throws DatabaseException, IllegalArgumentException {
            followsRepository.deleteFollow(new UserId(followsCommand.user_id()), new UserId(followsCommand.follows()));
    }
}
