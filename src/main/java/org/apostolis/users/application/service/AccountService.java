package org.apostolis.users.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.App;
import org.apostolis.common.TransactionUtils;
import org.apostolis.exception.AuthenticationException;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.security.TokenManager;
import org.apostolis.users.application.ports.in.*;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.domain.Role;
import org.apostolis.users.domain.User;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Account and authentication business logic
@RequiredArgsConstructor
public class AccountService implements AccountManagementUseCase {
    private final UserRepository repository;
    private final TokenManager tokenManager;
    private final PasswordEncoder passwordEncoder;
    private final TransactionUtils transactionUtils;

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Override
    public void registerUser(RegisterCommand command) throws Exception {
        TransactionUtils.ThrowingConsumer<Session,Exception> registerUser = (session) -> {
            String username = command.username();
            if(repository.getByUsername(username) != null){
                logger.warn("Username is already taken.");
                throw new IllegalArgumentException("Username is already taken.");
            }
            User user = new User(username, passwordEncoder.encodePassword(command.password()), command.role());
            repository.save(user);
            logger.info("User registered successfully");
        };
        transactionUtils.doInTransaction(registerUser);
    }

    @Override
    public String loginUser(LoginCommand loginCommand) throws Exception {
        TransactionUtils.ThrowingFunction<Session, String, Exception> loginUser = (session) -> {
            User user = repository.getByUsername(loginCommand.username());
            if (user.checkPassword(loginCommand.password(), passwordEncoder)) {
                String token = tokenManager.issueToken(loginCommand.username(), Role.valueOf(user.getRole()));
                logger.info("User logged in successfully");
                return token;
            } else {
                throw new AuthenticationException("Incorrect password or username");
            }
        };
        return transactionUtils.doInTransaction(loginUser);
    }

    @Override
    public void authenticate(String token) throws Exception {
            String cleanToken;
            if (token != null){
                cleanToken = token.substring(7);
            }else{
                logger.error("Token is missing from incoming request");
                throw new AuthenticationException("Authentication token missing");
            }
        TransactionUtils.ThrowingConsumer<Session,Exception> authenticateUser = (session) -> {
            User user = repository.getByUsername(tokenManager.extractUsername(cleanToken));
            user.authenticateUser(cleanToken, tokenManager);
            // Update the current user id
            App.currentUserId.set(repository.getUserIdFromUsername(user.getUsername()));
            logger.info("User authenticated");
        };
        transactionUtils.doInTransaction(authenticateUser);
    }
}
