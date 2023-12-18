package org.apostolis.users.application.ports.out;

import org.apostolis.exception.DatabaseException;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.domain.User;

import java.util.Optional;

public interface UserRepository {
    void save(User user);
    void saveFollowing(UserId user, UserId user_to_follow);
    void deleteFollowing(UserId user, UserId user_to_follow);

    Optional<User> findById(UserId user_id);
    User getByUsername(String username) throws DatabaseException;
    UserId getUserIdFromUsername(String username);
    String getUsernameFromId(UserId userId);
}
