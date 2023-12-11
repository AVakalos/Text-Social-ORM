package org.apostolis.users.application.ports.out;

import org.apostolis.exception.DatabaseException;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.domain.User;

public interface UserRepository {
    void save(User user);
    User getByUsername(String username) throws DatabaseException;
    UserId getUserIdFromUsername(String username);
    String getUsernameFromId(UserId userId);
}
