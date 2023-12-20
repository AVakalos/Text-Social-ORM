package org.apostolis.users.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.common.TransactionUtils;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.domain.User;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.application.ports.in.FollowsCommand;
import org.apostolis.users.application.ports.in.FollowsUseCase;
import org.hibernate.Session;

// Follows business logic
@RequiredArgsConstructor
public class FollowsService implements FollowsUseCase {

    private final UserRepository userRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public void followUser(FollowsCommand followsCommand) throws Exception {
        TransactionUtils.ThrowingConsumer<Session,Exception> followUsr = (session) -> {

            UserId user = followsCommand.user_id();
            UserId user_to_follow = followsCommand.follows();

            //Optional<User> current_user = userRepository.findById(user);
            //Optional<User> following_user = userRepository.findById(user_to_follow);

            User current_user = userRepository.fetchUserWithFollowingUsers(user);
            //current_user.getFollowing_users().forEach((c)->System.out.println("FOL: "+c));
            User following_user = userRepository.fetchUserWithFollowingUsers(user_to_follow);

            if(current_user == null || following_user == null){
                throw new IllegalArgumentException("Could not retrieve following users");
            }
            current_user.addFollowingUser(following_user);

            userRepository.saveFollowing(user,user_to_follow);
        };
        transactionUtils.doInTransaction(followUsr);
    }

    @Override
    public void unfollowUser(FollowsCommand followsCommand) throws Exception {
        TransactionUtils.ThrowingConsumer<Session,Exception> unfollowUsr = (session) -> {
            UserId user = followsCommand.user_id();
            UserId user_to_unfollow = followsCommand.follows();

//            Optional<User> current_user = userRepository.findById(user);
//            Optional<User> unfollow_user = userRepository.findById(user_to_unfollow);

            User current_user = userRepository.fetchUserWithFollowingUsers(user);
            //System.out.println("Current "+current_user.getId()+" username "+current_user.getUsername());
            //current_user.getFollowing_users().forEach((c)->System.out.println("FOL: "+c.getId()));


            User unfollow_user = userRepository.fetchUserWithFollowingUsers(user_to_unfollow);
            //System.out.println("Unfollow "+unfollow_user.getId()+" username "+unfollow_user.getUsername());

            if(current_user == null || unfollow_user == null){
                throw new IllegalArgumentException("Could not retrieve unfollowing users");
            }

            current_user.removeFollowingUser(unfollow_user);
            userRepository.deleteFollowing(user,user_to_unfollow);
        };
        transactionUtils.doInTransaction(unfollowUsr);
    }
}
