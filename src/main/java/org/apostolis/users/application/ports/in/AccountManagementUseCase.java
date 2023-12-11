package org.apostolis.users.application.ports.in;

import org.apostolis.exception.AuthenticationException;
import org.apostolis.exception.DatabaseException;

public interface AccountManagementUseCase {
    void registerUser(RegisterCommand command) throws Exception;
    String loginUser(LoginCommand loginCommand) throws Exception;
    void authenticate(String token) throws Exception;
}
