package org.apostolis.users.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.common.TransactionUtils;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.apostolis.users.domain.User;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.application.ports.in.FollowsCommand;
import org.apostolis.users.application.ports.in.FollowsUseCase;
import org.apostolis.users.application.ports.out.FollowViewsRepository;
import org.hibernate.Session;

// Follows business logic
@RequiredArgsConstructor
public class FollowsService implements FollowsUseCase {

    private final FollowViewsRepository followViewsRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public void followUser(FollowsCommand followsCommand) throws Exception {
        TransactionUtils.ThrowingConsumer<Session,Exception> followUsr = (session) -> {

            UserId user = followsCommand.user_id();
            UserId user_to_follow = followsCommand.follows();

            UserEntity current_user_entity = session.get(UserEntity.class, user.getUser_id());
            UserEntity following_user_entity = session.get(UserEntity.class, user_to_follow.getUser_id());
            if(following_user_entity == null){
                throw new IllegalArgumentException("Could not retrieve user: "+user_to_follow.getUser_id());
            }

            User current_user = current_user_entity.mapToDTO();
            User following_user = following_user_entity.mapToDTO();


            current_user.addFollowingUser(following_user);
            //following_user.addFollower(current_user);

            current_user_entity.addFollowing(following_user_entity);
            //following_user_entity.addFollower(current_user_entity);

            session.persist(current_user_entity);
            //session.persist(following_user_entity);
        };
        transactionUtils.doInTransaction(followUsr);
    }

    @Override
    public void unfollowUser(FollowsCommand followsCommand) throws Exception {
        TransactionUtils.ThrowingConsumer<Session,Exception> unfollowUsr = (session) -> {
            UserId user = followsCommand.user_id();
            UserId user_to_unfollow = followsCommand.follows();

            UserEntity current_user_entity = session.get(UserEntity.class, user.getUser_id());
            UserEntity unfollow_user_entity = session.get(UserEntity.class, user_to_unfollow.getUser_id());
            if(unfollow_user_entity == null){
                throw new IllegalArgumentException("Could not retrieve user: "+user_to_unfollow.getUser_id());
            }
            User current_user = current_user_entity.mapToDTO();
            User following_user = unfollow_user_entity.mapToDTO();

            current_user.removeFollowingUser(following_user);
            current_user_entity.removeFollowing(unfollow_user_entity);
            session.persist(current_user_entity);
        };
        transactionUtils.doInTransaction(unfollowUsr);
    }
}
