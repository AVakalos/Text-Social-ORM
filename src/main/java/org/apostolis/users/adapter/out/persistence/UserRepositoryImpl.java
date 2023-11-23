package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.NoResultException;
import org.apostolis.common.DbUtils;
import org.apostolis.common.HibernateUtil;
import org.apostolis.exception.DatabaseException;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.users.application.ports.in.RegisterCommand;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.domain.User;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserRepositoryImpl implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    @Override
    public void save(User user) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        sessionFactory.inTransaction(session ->
            session.persist(new UserEntity(user.username(),user.password(),user.role()))
        );
        logger.info("User registered");
    }

    @Override
    public User getByUsername(String username) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        UserEntity user;
        try {
            user = sessionFactory.fromTransaction(session ->
                    session.createSelectionQuery("from UserEntity where username = :username ", UserEntity.class)
                            .setParameter("username", username)
                            .getSingleResult());
        }catch(NoResultException e){
            return null;
        }
        return new User(user.getUsername(), user.getPassword(),user.getRole());
    }

    @Override
    public long getUserIdFromUsername(String username) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        UserEntity user = sessionFactory.fromTransaction(session ->
                session.createSelectionQuery("from UserEntity where username = :username ", UserEntity.class)
                .setParameter("username", username).getSingleResult());
        return user.getUser_id();
    }

    @Override
    public String getUsernameFromId(long userId) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        UserEntity user = sessionFactory.fromTransaction(session -> session.get(UserEntity.class,userId));
        return user.getUsername();
    }
}
