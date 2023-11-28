package org.apostolis.users.application.ports.in;

import org.apostolis.exception.AuthenticationException;

public interface LoginUseCase {
    String loginUser(LoginCommand loginCommand) throws AuthenticationException;
    void authenticate(String token) throws AuthenticationException;
}
