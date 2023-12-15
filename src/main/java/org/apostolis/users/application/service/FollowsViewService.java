package org.apostolis.users.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.common.PageRequest;
import org.apostolis.common.TransactionUtils;
import org.apostolis.users.domain.FollowsView;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.apostolis.users.application.ports.out.FollowViewsRepository;
import org.hibernate.Session;

// Follows views business logic
@RequiredArgsConstructor
public class FollowsViewService implements GetFollowersAndUsersToFollowUseCase {

    private final FollowViewsRepository followViewsRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public FollowsView getFollowers(UserId user, int pageNum, int pageSize) throws Exception {
        TransactionUtils.ThrowingFunction<Session, FollowsView, Exception> getFollowers = (session) ->
            new FollowsView(followViewsRepository.getFollowers(user, new PageRequest(pageNum, pageSize)));
        return transactionUtils.doInTransaction(getFollowers);
    }

    @Override
    public FollowsView getUsersToFollow(UserId user, int pageNum, int pageSize) throws Exception {
        TransactionUtils.ThrowingFunction<Session, FollowsView, Exception> getUsersToFollow = (session) ->
           new FollowsView(followViewsRepository.getUsersToFollow(user, new PageRequest(pageNum, pageSize)));
        return transactionUtils.doInTransaction(getUsersToFollow);
    }
}
