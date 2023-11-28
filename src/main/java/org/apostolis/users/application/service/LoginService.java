package org.apostolis.users.application.service;

import org.apostolis.App;
import org.apostolis.exception.AuthenticationException;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.security.TokenManager;
import org.apostolis.users.application.ports.in.LoginUseCase;
import org.apostolis.users.application.ports.in.LoginCommand;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.domain.Role;
import org.apostolis.users.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginService implements LoginUseCase {
    private final UserRepository repository;
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);

    public LoginService(UserRepository repository, TokenManager tokenManager, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.tokenManager = tokenManager;
        this.passwordEncoder = passwordEncoder;
    }

    private boolean checkPassword(String hashedPassword, String insertedPassword){
        try {
            return passwordEncoder.checkPassword(insertedPassword, hashedPassword);
        }catch (Exception e){
            logger.error("Check password: "+e.getMessage());
            return false;
        }
    }

    @Override
    public String loginUser(LoginCommand loginCommand) throws AuthenticationException{
        String inserted_username = loginCommand.username();
        String inserted_password = loginCommand.password();
        User user = repository.getByUsername(inserted_username);

        if(checkPassword(user.password(),inserted_password)){
            String token = tokenManager.issueToken(inserted_username, Role.valueOf(user.role()));
            logger.info("User logged in successfully");
            return token;
        }else{
            throw new AuthenticationException("Incorrect password or username");
        }
    }

    @Override
    public void authenticate(String token) throws AuthenticationException {
        if (token != null){
            token = token.substring(7);
        }else{
            logger.error("Token is missing from incoming request");
            throw new AuthenticationException("Authentication token missing");
        }
        boolean isTokenValid = tokenManager.validateToken(token);
        if (!isTokenValid){
            logger.error("Token is invalid");
            throw new AuthenticationException("Authentication token is invalid");
        }
        // Update the current user id
        App.currentUserId.set(repository.getUserIdFromUsername(tokenManager.extractUsername(token)));
        logger.info("User authenticated");
    }

}
