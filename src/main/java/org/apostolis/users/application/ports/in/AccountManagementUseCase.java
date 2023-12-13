package org.apostolis.users.application.ports.in;

public interface AccountManagementUseCase {
    void registerUser(RegisterCommand command) throws Exception;
    String loginUser(LoginCommand loginCommand) throws Exception;
    void authenticate(String token) throws Exception;
}
