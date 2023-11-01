package org.apostolis.users.application.service;

import org.apostolis.exception.DatabaseException;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.users.application.ports.in.RegisterUseCase;
import org.apostolis.users.application.ports.in.RegisterCommand;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterService implements RegisterUseCase {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(RegisterService.class);

    public RegisterService(UserRepository repository, PasswordEncoder passwordEncoder){
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void registerUser(RegisterCommand command) throws IllegalArgumentException, DatabaseException {
        if(repository.getByUsername(command.username()) != null){
            logger.warn("Username is already taken.");
            throw new IllegalArgumentException("Username is already taken.");
        }
        User user = new User(command.username(), command.password(), command.role());
        repository.save(user, passwordEncoder);
        logger.info("User registered successfully");
    }
}
