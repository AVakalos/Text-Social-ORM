package org.apostolis.users.application.ports.out;

import org.apostolis.security.PasswordEncoder;
import org.apostolis.users.application.ports.in.RegisterCommand;
import org.apostolis.users.domain.User;

public interface UserRepository {
    void save(RegisterCommand command, PasswordEncoder passwordEncoder);
    User getByUsername(String username) throws Exception;
    int getUserIdFromUsername(String username);
    String getUsernameFromId(int userId);
}
