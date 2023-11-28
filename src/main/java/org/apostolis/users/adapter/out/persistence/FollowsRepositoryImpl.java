package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.Tuple;
import org.apostolis.AppConfig;
import org.apostolis.common.PageRequest;
import org.apostolis.exception.DatabaseException;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.domain.UserDTO;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowsRepositoryImpl implements FollowsRepository {

    private static final Logger logger = LoggerFactory.getLogger(FollowsRepositoryImpl.class);

    @Override
    public void saveFollow(UserId user, UserId user_to_follow) throws IllegalArgumentException, DatabaseException{
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        try {
            sessionFactory.inTransaction(session -> {
                UserEntity current_user = session.get(UserEntity.class, user.getUser_id());
                UserEntity following_user = session.get(UserEntity.class, user_to_follow.getUser_id());
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
    public void deleteFollow(UserId user, UserId userToUnfollow) throws IllegalArgumentException, DatabaseException {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        try {
            sessionFactory.inTransaction(session -> {
                UserEntity current_user = session.get(UserEntity.class, user.getUser_id());
                UserEntity following_user = session.get(UserEntity.class, userToUnfollow.getUser_id());
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
    public Map<UserId,UserDTO> getFollowers(UserId user_id, PageRequest req) throws DatabaseException {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        String followersQuery = """
                        select f.user_id as uid, f.username as username
                        from UserEntity u join u.followers f
                        where u.user_id = :userId""";
        try {
            return processQueryResults(user_id, req, sessionFactory, followersQuery);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve followers",e);
        }
    }

    private Map<UserId, UserDTO> processQueryResults(UserId user_id, PageRequest req, SessionFactory sessionFactory, String followersQuery) {
        return sessionFactory.fromTransaction(session -> {
            List<Tuple> followers = session.createSelectionQuery(followersQuery, Tuple.class)
                    .setParameter("userId", user_id.getUser_id())
                    .setFirstResult(req.pageNumber() * req.pageSize())
                    .setMaxResults(req.pageSize())
                    .getResultList();

            Map<UserId,UserDTO> results = new HashMap<>();
            for (Tuple follower : followers) {
                results.put(new UserId((Long)follower.get("uid")), new UserDTO((String)follower.get("username")));
            }
            return results;
        });
    }

    @Override
    public Map<UserId,UserDTO> getFollowing(UserId user_id, PageRequest req) throws DatabaseException {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        String followingQuery = """
                        select f.user_id as uid, f.username as username
                        from UserEntity u join u.following f
                        where u.user_id = :userId""";
        try {
            return processQueryResults(user_id, req, sessionFactory, followingQuery);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve following users",e);
        }
    }

    @Override
    public Map<UserId,UserDTO> getUsersToFollow(UserId user_id, PageRequest req) throws DatabaseException {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        String usersToFollowQuery = """
                    select user_id as uid, username as username
                    from UserEntity
                    where user_id != :user and user_id not in(
                        select f.user_id
                        from UserEntity u join u.following f
                        where u.user_id = :user)""";
        try {
            return sessionFactory.fromTransaction(session -> {
                List<Tuple> usersToFollow =
                        session.createSelectionQuery(usersToFollowQuery, Tuple.class)
                                .setParameter("user", user_id.getUser_id())
                                .setFirstResult(req.pageNumber() * req.pageSize())
                                .setMaxResults(req.pageSize())
                                .getResultList();
                Map<UserId,UserDTO> usersToFollowMap = new HashMap<>();
                System.out.println(user_id.getUser_id());
                for (Tuple usr : usersToFollow) {
                    System.out.println(usr);
                    usersToFollowMap.put(new UserId((Long) usr.get("uid")), new UserDTO((String) usr.get("username")));
                }
                return usersToFollowMap;
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve users to follow",e);
        }
    }
}
