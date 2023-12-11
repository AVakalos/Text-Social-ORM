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

// User database CRUD operations
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final TransactionUtils transactionUtils;
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Override
    public void save(User user) {
        TransactionUtils.ThrowingConsumer<Session,Exception> saveTask = (session) -> {
            System.out.println("Session: "+session);
            session.persist(new UserEntity(user.getUsername(),user.getPassword(),user.getRole()));
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
