package org.apostolis.users.application.service;

import org.apostolis.common.PageRequest;
import org.apostolis.users.adapter.out.persistence.UserId;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.domain.UserDTO;
import java.util.Map;

public class GetFollowsService implements GetFollowersAndUsersToFollowUseCase {

    private final FollowsRepository followsRepository;

    public GetFollowsService(FollowsRepository followsRepository) {
        this.followsRepository = followsRepository;
    }

    @Override
    public Map<UserId, UserDTO> getFollowers(UserId user, int pageNum, int pageSize) {
        return followsRepository.getFollowers(user,new PageRequest(pageNum,pageSize));
    }

    @Override
    public Map<UserId, UserDTO> getUsersToFollow(UserId user, int pageNum, int pageSize) {
        return followsRepository.getUsersToFollow(user,new PageRequest(pageNum,pageSize));
    }
}
