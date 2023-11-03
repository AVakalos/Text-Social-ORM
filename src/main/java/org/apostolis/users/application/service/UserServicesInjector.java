package org.apostolis.users.application.service;

import org.apostolis.security.PasswordEncoder;
import org.apostolis.security.TokenManager;
import org.apostolis.users.application.ports.in.FollowsUseCase;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.apostolis.users.application.ports.in.LoginUseCase;
import org.apostolis.users.application.ports.in.RegisterUseCase;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.application.ports.out.UserRepository;


public class UserServicesInjector {

    private final UserRepository userRepository;
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;

    private final FollowsRepository followsRepository;

    public UserServicesInjector(UserRepository userRepository,
                                TokenManager tokenManager,
                                PasswordEncoder passwordEncoder,
                                FollowsRepository followsRepository) {
        this.userRepository = userRepository;
        this.tokenManager = tokenManager;
        this.passwordEncoder = passwordEncoder;
        this.followsRepository = followsRepository;
    }

    public RegisterUseCase getRegisterService(){
        return new RegisterService(userRepository, passwordEncoder);
    }

    public LoginUseCase getLoginService(){
        return new LoginService(userRepository, tokenManager, passwordEncoder);
    }

    public FollowsUseCase getFollowsService(){
        return new FollowsService(followsRepository);
    }

    public GetFollowersAndUsersToFollowUseCase getGetFollowsService(){
        return new GetFollowsService(followsRepository);
    }

}
