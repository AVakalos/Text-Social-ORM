package org.apostolis.users.application.ports.in;

public interface LoginUseCase {
    String loginUser(LoginCommand loginCommand) throws Exception;

    void authenticate(String token) throws Exception;
}
