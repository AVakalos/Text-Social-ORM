package org.apostolis.users.application.service;

import org.apostolis.common.PageRequest;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.domain.UserInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GetFollowsService implements GetFollowersAndUsersToFollowUseCase {

    private final FollowsRepository followsRepository;

    public GetFollowsService(FollowsRepository followsRepository) {
        this.followsRepository = followsRepository;
    }

    @Override
    public HashMap<Long, String> getFollowers(long user, int pageNum, int pageSize) {
        return followsRepository.getFollowers(user,new PageRequest(pageNum,pageSize));
    }

    @Override
    public HashMap<Long, String> getUsersToFollow(long user, int pageNum, int pageSize) {
        HashMap<Long, String> requestOutput = new HashMap<>();
        List<UserInfo> usersToFollow = followsRepository.getUsersToFollow(user,new PageRequest(pageNum,pageSize));
        for(UserInfo u: usersToFollow){
            requestOutput.put(u.id(), u.username());
        }
        return requestOutput;
    }
}
