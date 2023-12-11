package org.apostolis.users.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.common.PageRequest;
import org.apostolis.common.TransactionUtils;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.domain.UserDTO;
import org.hibernate.Session;

import java.util.Map;

// Follows views business logic
@RequiredArgsConstructor
public class FollowsViewService implements GetFollowersAndUsersToFollowUseCase {

    private final FollowsRepository followsRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public Map<UserId, UserDTO> getFollowers(UserId user, int pageNum, int pageSize) throws Exception {
        TransactionUtils.ThrowingFunction<Session, Map<UserId,UserDTO>, Exception> getFollowers = (session) ->
                followsRepository.getFollowers(user,new PageRequest(pageNum,pageSize));
        return transactionUtils.doInTransaction(getFollowers);
    }

    @Override
    public Map<UserId, UserDTO> getUsersToFollow(UserId user, int pageNum, int pageSize) throws Exception {
        TransactionUtils.ThrowingFunction<Session, Map<UserId,UserDTO>, Exception> getUsersToFollow = (session) ->
                followsRepository.getUsersToFollow(user,new PageRequest(pageNum,pageSize));
        return transactionUtils.doInTransaction(getUsersToFollow);
    }
}
