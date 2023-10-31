package org.apostolis.users.application.service;

import org.apostolis.App;
import org.apostolis.users.application.ports.in.FollowCommand;
import org.apostolis.users.application.ports.in.FollowUseCase;
import org.apostolis.users.application.ports.out.FollowingRepository;
import org.apostolis.users.application.ports.out.UserRepository;

public class FollowingService implements FollowUseCase {

    private final FollowingRepository followingRepository;

    private final UserRepository userRepository;

    public FollowingService(FollowingRepository followingRepository, UserRepository userRepository) {
        this.followingRepository = followingRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void followUser(FollowCommand followCommand) throws Exception{
//        int authenticated_user_id = requestValidationService.extractUserId(followCommand.token());
//        if (followCommand.user() != authenticated_user_id){
//            throw new UnauthorizedResponse("Your request id does not match with your authentication id");
//        }

        if (App.currentUserId.get() != followCommand.follows()){
            followingRepository.saveFollow(followCommand);

        }else{
            throw new IllegalArgumentException("You can't follow yourself");
        }

    }
}
