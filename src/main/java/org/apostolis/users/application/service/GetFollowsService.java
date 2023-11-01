package org.apostolis.users.application.service;

import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.apostolis.users.application.ports.out.FollowsRepository;

import java.util.ArrayList;

public class GetFollowsService implements GetFollowersAndUsersToFollowUseCase {

    private final FollowsRepository followsRepository;

    public GetFollowsService(FollowsRepository followsRepository) {
        this.followsRepository = followsRepository;
    }

    @Override
    public ArrayList<String> getFollowers(GetFollowsQuery query) {
        return followsRepository.getFollowers(query.user());
    }

    @Override
    public ArrayList<String> getUsersToFollow(GetFollowsQuery query) {
        return followsRepository.getUsersToFollow(query.user());
    }
}
