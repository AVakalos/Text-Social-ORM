package org.apostolis.users.application.service;

import org.apostolis.App;
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
        int user = followsCommand.follows();
        if (App.currentUserId.get() != user){
            followsRepository.saveFollow(user);
        }else{
            throw new IllegalArgumentException("You can't follow yourself");
        }
    }

    @Override
    public void unfollowUser(FollowsCommand followsCommand) throws DatabaseException, IllegalArgumentException {
            followsRepository.deleteFollow(followsCommand.follows());
    }
}
