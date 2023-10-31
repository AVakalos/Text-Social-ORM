package org.apostolis.users.application.ports.in;

public interface RegisterUseCase {
    void registerUser(RegisterCommand command) throws Exception;
}
