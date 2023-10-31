package org.apostolis.users.application.service;

import org.apostolis.security.PasswordEncoder;
import org.apostolis.users.application.ports.in.RegisterUseCase;
import org.apostolis.users.application.ports.in.RegisterCommand;
import org.apostolis.users.application.ports.out.UserRepository;
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
    public void registerUser(RegisterCommand command) throws Exception {
        if(repository.getByUsername(command.username()) != null){
            logger.warn("Username is already taken.");
            throw new IllegalArgumentException("Username is already taken.");
        }
        repository.save(command, passwordEncoder);
        logger.info("User registered successfully");
    }
}
