package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.apostolis.common.TransactionUtils;
import org.apostolis.exception.DatabaseException;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.domain.User;
import org.apostolis.users.domain.UserId;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

// User database CRUD operations
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final TransactionUtils transactionUtils;
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Override
    public void save(User user) {
        TransactionUtils.ThrowingConsumer<Session,Exception> saveTask = (session) -> {
            session.persist(UserEntity.mapToEntity(user));
        };
        try{
            transactionUtils.doInTransaction(saveTask);
            logger.info("User registered");
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not save new user",e);
        }
    }

    @Override
    public void saveFollowing(UserId user, UserId user_to_follow) {
        TransactionUtils.ThrowingConsumer<Session, Exception> saveFollowingTask = (session) -> {
            UserEntity userEntity = session.find(UserEntity.class, user.getValue());
            UserEntity userToFollowEntity = session.find(UserEntity.class, user_to_follow.getValue());
            userEntity.addFollowing(userToFollowEntity);
            session.persist(userEntity);
        };
        try{
            transactionUtils.doInTransaction(saveFollowingTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not save Following",e);
        }
    }

    @Override
    public void deleteFollowing(UserId user, UserId user_to_follow) {
        TransactionUtils.ThrowingConsumer<Session, Exception> deleteFollowTask = (session) -> {
            UserEntity userEntity = session.find(UserEntity.class, user.getValue());
            UserEntity userToFollowEntity = session.find(UserEntity.class, user_to_follow.getValue());
            userEntity.removeFollowing(userToFollowEntity);
            session.persist(userEntity);
        };
        try{
            transactionUtils.doInTransaction(deleteFollowTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not delete follow",e);
        }
    }

    @Override
    public Optional<User> findById(UserId user_id) {
        TransactionUtils.ThrowingFunction<Session, Optional<User>, Exception> findByIdTask = (session) -> {
           UserEntity userEntity = session.find(UserEntity.class, user_id.getValue());
           return Optional.ofNullable(userEntity.mapToDomain());
        };
        try {
            return transactionUtils.doInTransaction(findByIdTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve user id by username",e);
        }
    }

    @Override
    public User getByUsername(String username) {
        TransactionUtils.ThrowingFunction<Session, User, Exception> getByUsernameTask = (session) -> {
            try{
                String query = "select username, password, role from UserEntity where username = :username";
                return session.createSelectionQuery(query, User.class)
                        .setParameter("username", username)
                        .getSingleResult();
            }catch(NoResultException e) {
                logger.info("User: " + username + " is not already stored in the database.");
                return null;
            }
        };
        try {
            return transactionUtils.doInTransaction(getByUsernameTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve User by username",e);
        }
    }

    @Override
    public UserId getUserIdFromUsername(String username) {
        TransactionUtils.ThrowingFunction<Session, UserId, Exception> getUserIdFromUsernameTask = (session) -> {
            String query = "select user_id from UserEntity where username = :username";
            Long user_id = session.createSelectionQuery(query, Long.class)
                    .setParameter("username", username)
                    .getSingleResult();
            return new UserId(user_id);
        };
        try {
            return transactionUtils.doInTransaction(getUserIdFromUsernameTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve user id by username",e);
        }
    }

    @Override
    public String getUsernameFromId(UserId userId) {
        TransactionUtils.ThrowingFunction<Session, String, Exception> getUsernameFromIdTask = (session) -> {
            String query = "select username from UserEntity where user_id = :userId";
            return session.createSelectionQuery(query, String.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        };
        try {
            return transactionUtils.doInTransaction(getUsernameFromIdTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve username by id",e);
        }
    }
}
