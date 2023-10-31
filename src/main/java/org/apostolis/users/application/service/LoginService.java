package org.apostolis.users.application.service;

import io.javalin.http.ForbiddenResponse;
import org.apostolis.App;
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

    private boolean checkPassword(String username, String password){
        try {
            User user = repository.getByUsername(username);
            String hashed_password = user.password();
            return passwordEncoder.checkPassword(password, hashed_password);
        }catch (Exception e){
            logger.error("Username: "+username+" not found in the database.");
            return false;
        }
    }

    @Override
    public String loginUser(LoginCommand loginCommand) throws Exception{
        String inserted_username = loginCommand.username();
        String inserted_password = loginCommand.password();
        User user = repository.getByUsername(inserted_username);
        if(checkPassword(inserted_username,inserted_password)){
            String token = tokenManager.issueToken(inserted_username, Role.valueOf(user.role()));
            logger.info("User logged in successfully");
            return token;
        }else{
            throw new Exception("Incorrect password or username");
        }
    }

    @Override
    public void authenticate(String token) throws Exception {
        if (token != null){
            token = token.substring(7);
        }else{
            logger.error("Token is missing from incoming request");
            throw new Exception("Authentication token missing");
        }
        boolean isTokenValid = tokenManager.validateToken(token);
        if (!isTokenValid){
            logger.error("Token is invalid");
            throw new Exception("Authentication token is invalid");
        }
        App.currentUserId.set(repository.getUserIdFromUsername(tokenManager.extractUsername(token)));
        logger.info("User authenticated");
    }


}
