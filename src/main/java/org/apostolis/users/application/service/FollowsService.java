package org.apostolis.users.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.common.TransactionUtils;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.application.ports.in.FollowsCommand;
import org.apostolis.users.application.ports.in.FollowsUseCase;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.hibernate.Session;

// Follows business logic
@RequiredArgsConstructor
public class FollowsService implements FollowsUseCase {

    private final FollowsRepository followsRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public void followUser(FollowsCommand followsCommand) throws Exception {
        TransactionUtils.ThrowingConsumer<Session,Exception> followUsr = (session) -> {
            UserId user = followsCommand.user_id();
            UserId user_to_follow = followsCommand.follows();
            if (!user.equals(user_to_follow)) {
                followsRepository.saveFollow(user, user_to_follow);
            } else {
                throw new IllegalArgumentException("You can't follow yourself");
            }
        };
        transactionUtils.doInTransaction(followUsr);
    }

    @Override
    public void unfollowUser(FollowsCommand followsCommand) throws Exception {
        TransactionUtils.ThrowingConsumer<Session,Exception> unfollowUsr = (session) ->
                followsRepository.deleteFollow(followsCommand.user_id(), followsCommand.follows());
        transactionUtils.doInTransaction(unfollowUsr);
    }
}
