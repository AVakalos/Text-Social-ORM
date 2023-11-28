package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.NoResultException;
import org.apostolis.AppConfig;
import org.apostolis.exception.DatabaseException;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.domain.User;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class UserRepositoryImpl implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Override
    public void save(User user) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        sessionFactory.inTransaction(session ->
            session.persist(new UserEntity(user.username(),user.password(),user.role()))
        );
        logger.info("User registered");
    }

    @Override
    public User getByUsername(String username) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        try {
            return sessionFactory.fromTransaction(session -> {
                String query = "select username, password, role from UserEntity where username = :username";
                return session.createSelectionQuery(query, User.class)
                        .setParameter("username", username)
                        .getSingleResult();
            });
        }catch(NoResultException e){
            logger.info("User: "+username+" is not already stored in the database.");
            return null;
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve User by username",e);
        }
    }

    @Override
    public UserId getUserIdFromUsername(String username) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        try {
            return sessionFactory.fromTransaction(session -> {
                String query = "select user_id from UserEntity where username = :username";
                Long user_id = session.createSelectionQuery(query, Long.class)
                        .setParameter("username", username)
                        .getSingleResult();
                return new UserId(user_id);
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve user id by username",e);
        }
    }

    @Override
    public String getUsernameFromId(UserId userId) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        try {
            return sessionFactory.fromTransaction(session -> {
                String query = "select username from UserEntity where user_id = :userId";
                return session.createSelectionQuery(query, String.class)
                        .setParameter("userId", userId.getUser_id())
                        .getSingleResult();
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve username by id",e);
        }
    }
}
