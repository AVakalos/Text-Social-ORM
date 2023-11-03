package org.apostolis.users.application.service;

import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.apostolis.users.application.ports.out.FollowsRepository;

import java.util.ArrayList;
import java.util.HashMap;

public class GetFollowsService implements GetFollowersAndUsersToFollowUseCase {

    private final FollowsRepository followsRepository;

    public GetFollowsService(FollowsRepository followsRepository) {
        this.followsRepository = followsRepository;
    }

    @Override
    public HashMap<Integer,String> getFollowers(int user) {
        return followsRepository.getFollowers(user);
    }

    @Override
    public HashMap<Integer,String> getUsersToFollow(int user) {
        return followsRepository.getUsersToFollow(user);
    }
}
