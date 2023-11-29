package org.apostolis.users.application.service;

import org.apostolis.App;
import org.apostolis.exception.AuthenticationException;
import org.apostolis.exception.DatabaseException;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.security.TokenManager;
import org.apostolis.users.application.ports.in.*;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.domain.Role;
import org.apostolis.users.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Account and authentication business logic
public class AccountService implements AccountManagementUseCase {
    private final UserRepository repository;
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(UserRepository repository, TokenManager tokenManager, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.tokenManager = tokenManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registerUser(RegisterCommand command) throws IllegalArgumentException, DatabaseException {
        if(repository.getByUsername(command.username()) != null){
            logger.warn("Username is already taken.");
            throw new IllegalArgumentException("Username is already taken.");
        }
        User user = new User(command.username(), passwordEncoder.encodePassword(command.password()), command.role());
        repository.save(user);
        logger.info("User registered successfully");
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
    public String loginUser(LoginCommand loginCommand) throws AuthenticationException {
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
