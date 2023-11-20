package org.apostolis.users.application.service;

import org.apostolis.exception.DatabaseException;
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
        long user = followsCommand.user();
        long user_to_follow = followsCommand.follows();
        if (user != user_to_follow){
            followsRepository.saveFollow(user, user_to_follow);
        }else{
            throw new IllegalArgumentException("You can't follow yourself");
        }
    }

    @Override
    public void unfollowUser(FollowsCommand followsCommand) throws DatabaseException, IllegalArgumentException {
            followsRepository.deleteFollow(followsCommand.user(), followsCommand.follows());
    }
}
