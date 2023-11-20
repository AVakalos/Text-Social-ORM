package org.apostolis.users.adapter.out.persistence;

import org.apostolis.common.HibernateUtil;
import org.apostolis.exception.DatabaseException;
import org.apostolis.users.application.ports.out.FollowsRepository;
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
        sessionFactory.inTransaction(session -> {
            UserEntity current_user = session.get(UserEntity.class,user);
            UserEntity following_user = session.get(UserEntity.class,user_to_follow);
            if(following_user == null){
                throw new IllegalArgumentException("You already follow this user or the user does not exist");
            }
            current_user.addFollowing(following_user);
            session.merge(current_user);
        });
    }

    @Override
    public void deleteFollow(long user, long userToUnfollow) throws IllegalArgumentException, DatabaseException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        sessionFactory.inTransaction(session -> {
            UserEntity current_user = session.get(UserEntity.class, user);
            UserEntity following_user = session.get(UserEntity.class, userToUnfollow);
            if(following_user == null){
                throw new IllegalArgumentException("You were not following this user or user does not exist");
            }
            current_user.removeFollowing(following_user);
            session.merge(current_user);
        });
    }

    @Override
    public HashMap<Long, String> getFollowers(long userId) throws DatabaseException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        return sessionFactory.fromTransaction(session -> {
            UserEntity user = session.get(UserEntity.class,userId);
            Set<UserEntity> userFollowers = user.getFollowers();
            HashMap<Long, String> results = new HashMap<>();
            for(UserEntity follower: userFollowers){
                results.put(follower.getUser_id(), follower.getUsername());
            }
            return results;
        });
    }

    @Override
    public HashMap<Long, String> getFollowing(long userId) throws DatabaseException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        return sessionFactory.fromTransaction(session -> {
            UserEntity user = session.get(UserEntity.class,userId);
            Set<UserEntity> userFollowing = user.getFollowing();
            HashMap<Long, String> results = new HashMap<>();
            for(UserEntity followingUser: userFollowing){
                results.put(followingUser.getUser_id(), followingUser.getUsername());
            }
            return results;
        });
    }

    @Override
    public HashMap<Long, String> getUsersToFollow(long user) throws DatabaseException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        StringBuilder query = new StringBuilder();
        query.append("""
                    select user_id, username\s
                    from UserEntity\s
                    where user_id != :user and user_id not in(
                        select element(following).user_id from UserEntity)
                """);
        return sessionFactory.fromTransaction(session -> {
            List<Object[]> usersToFollow =
                    session.createSelectionQuery(query.toString(), Object[].class)
                            .setParameter("user",user)
                            .getResultList();
            HashMap<Long, String> usersToFollowMap = new HashMap<>();
            for(var usr: usersToFollow){
                usersToFollowMap.put((Long) usr[0], (String) usr[1]);
            }
            return usersToFollowMap;
        });
    }
}
