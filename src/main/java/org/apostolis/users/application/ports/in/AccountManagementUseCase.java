package org.apostolis.users.application.ports.in;

import org.apostolis.exception.AuthenticationException;
import org.apostolis.exception.DatabaseException;

public interface AccountManagementUseCase {
    void registerUser(RegisterCommand command) throws DatabaseException, IllegalArgumentException;
    String loginUser(LoginCommand loginCommand) throws AuthenticationException;
    void authenticate(String token) throws AuthenticationException;
}
