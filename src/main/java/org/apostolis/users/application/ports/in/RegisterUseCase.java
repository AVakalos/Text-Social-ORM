package org.apostolis.users.application.ports.in;

import org.apostolis.exception.DatabaseException;

public interface RegisterUseCase {
    void registerUser(RegisterCommand command) throws DatabaseException, IllegalArgumentException;
}
