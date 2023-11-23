package org.apostolis.users.adapter.out.persistence;

import org.apostolis.common.HibernateUtil;
import org.apostolis.common.PageRequest;
import org.apostolis.exception.DatabaseException;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.domain.UserInfo;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class FollowsRepositoryImpl implements FollowsRepository {

    private static final Logger logger = LoggerFactory.getLogger(FollowsRepositoryImpl.class);

    @Override
    public void saveFollow(long user, long user_to_follow) throws IllegalArgumentException, DatabaseException{
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try {
            sessionFactory.inTransaction(session -> {
                UserEntity current_user = session.get(UserEntity.class, user);
                UserEntity following_user = session.get(UserEntity.class, user_to_follow);
                if (following_user == null) {
                    throw new IllegalArgumentException("You already follow this user or the user does not exist");
                }
                current_user.addFollowing(following_user);
                session.merge(current_user);
            });
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not save follow relationship",e);
        }
    }

    @Override
    public void deleteFollow(long user, long userToUnfollow) throws IllegalArgumentException, DatabaseException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try {
            sessionFactory.inTransaction(session -> {
                UserEntity current_user = session.get(UserEntity.class, user);
                UserEntity following_user = session.get(UserEntity.class, userToUnfollow);
                if (following_user == null) {
                    throw new IllegalArgumentException("You were not following this user or user does not exist");
                }
                current_user.removeFollowing(following_user);
                session.merge(current_user);
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not delete follow relationship",e);
        }
    }

    @Override
    public HashMap<Long, String> getFollowers(long userId, PageRequest req) throws DatabaseException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try {
            return sessionFactory.fromTransaction(session -> {
                List<UserEntity> followers = session.createSelectionQuery(
                                "select followers from UserEntity where user_id = :userId", UserEntity.class)
                        .setParameter("userId", userId)
                        .setFirstResult(req.pageNumber() * req.pageSize())
                        .setMaxResults(req.pageSize())
                        .getResultList();

                HashMap<Long, String> results = new HashMap<>();
                for (UserEntity follower : followers) {
                    results.put(follower.getUser_id(), follower.getUsername());
                }
                return results;
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve followers",e);
        }
    }

    @Override
    public HashMap<Long, String> getFollowing(long userId, PageRequest req) throws DatabaseException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try {
            return sessionFactory.fromTransaction(session -> {
                List<UserEntity> followingUsers = session.createSelectionQuery(
                                "select following from UserEntity where user_id = :userId", UserEntity.class)
                        .setParameter("userId", userId)
                        .setFirstResult(req.pageNumber() * req.pageSize())
                        .setMaxResults(req.pageSize())
                        .getResultList();
                HashMap<Long, String> results = new HashMap<>();
                for (UserEntity followingUser : followingUsers) {
                    results.put(followingUser.getUser_id(), followingUser.getUsername());
                }
                return results;
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve following users",e);
        }
    }

    @Override
    public List<UserInfo> getUsersToFollow(long user, PageRequest req) throws DatabaseException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        StringBuilder query = new StringBuilder();
        query.append("""
                    select new org.apostolis.users.domain.UserInfo(user_id, username)
                    from UserEntity
                    where user_id != :user and user_id not in(
                        select element(following).user_id from UserEntity)
                """);
        try {
            return sessionFactory.fromTransaction(session -> {
                List<UserInfo> usersToFollow =
                        session.createSelectionQuery(query.toString(), UserInfo.class)
                                .setParameter("user", user)
                                .setFirstResult(req.pageNumber() * req.pageSize())
                                .setMaxResults(req.pageSize())
                                .getResultList();
//                HashMap<Long, String> usersToFollowMap = new HashMap<>();
//                for (var usr : usersToFollow) {
//                    usersToFollowMap.put((Long) usr[0], (String) usr[1]);
//                }
//                return usersToFollowMap;
                return usersToFollow;
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve users to follow",e);
        }
    }
}
