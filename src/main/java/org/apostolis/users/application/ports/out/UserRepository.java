package org.apostolis.users.application.ports.out;

import org.apostolis.exception.DatabaseException;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.users.application.ports.in.RegisterCommand;
import org.apostolis.users.domain.User;

public interface UserRepository {
    void save(User user, PasswordEncoder passwordEncoder);
    User getByUsername(String username) throws DatabaseException;
    int getUserIdFromUsername(String username);
    String getUsernameFromId(int userId);
}
