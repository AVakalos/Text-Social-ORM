package org.apostolis.users.adapter.out.persistence;

import org.apostolis.common.DbUtils;
import org.apostolis.common.HibernateUtil;
import org.apostolis.exception.DatabaseException;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.parser.Entity;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class FollowsRepositoryImpl implements FollowsRepository {

    private static final Logger logger = LoggerFactory.getLogger(FollowsRepositoryImpl.class);
    private final DbUtils dbUtils;

    public FollowsRepositoryImpl(DbUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public void saveFollow(int user, int user_to_follow) throws IllegalArgumentException, DatabaseException{
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        sessionFactory.inTransaction(session -> {
            UserEntity current_user = session.get(UserEntity.class,user);
            UserEntity following_user = session.get(UserEntity.class,user_to_follow);
            if(following_user == null){
                throw new IllegalArgumentException("You already follow this user or the user does not exist");
            }
            current_user.addFollowing(following_user);
            following_user.addFollower(current_user);
            session.merge(current_user);
            session.merge(following_user);
        });
    }

    @Override
    public void deleteFollow(int user, int userToUnfollow) throws IllegalArgumentException, DatabaseException {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        sessionFactory.inTransaction(session -> {
            UserEntity current_user = session.get(UserEntity.class, user);
            UserEntity following_user = session.get(UserEntity.class, userToUnfollow);
            if(following_user == null){
                throw new IllegalArgumentException("You were not following this user or user does not exist");
            }
            current_user.removeFollowing(following_user);
            following_user.removeFollower(current_user);
            session.merge(current_user);
            session.merge(following_user);
        });
    }

    @Override
    public HashMap<Integer,String> getFollowers(int user) throws DatabaseException {
        DbUtils.ThrowingFunction<Connection, HashMap<Integer,String>, Exception> get_followers = (conn) -> {
            String query = "WITH follower_ids AS (SELECT DISTINCT user_id " +
                    "   FROM followers " +
                    "   WHERE following_id = ?) " +
                    "SELECT u.user_id AS follower_id, username " +
                    "FROM users AS u " +
                    "INNER JOIN follower_ids AS f " +
                    "ON u.user_id = f.user_id;";
            HashMap<Integer,String> results = new HashMap<>();
            try(PreparedStatement pst = conn.prepareStatement(query)){
                pst.setInt(1,user);
                ResultSet rs = pst.executeQuery();
                while(rs.next()){
                    int id = rs.getInt("follower_id");
                    String username = rs.getString("username");
                    results.put(id, username);
                }
            }
            return results;
        };
        try{
            return dbUtils.doInTransaction(get_followers);
        }catch(Exception e){
            logger.error("No followers for user: "+user);
            throw new DatabaseException("No followers for user: "+user,e);
        }
    }

    @Override
    public HashMap<Integer, String> getFollowing(int user) throws DatabaseException {
        DbUtils.ThrowingFunction<Connection, HashMap<Integer,String>, Exception> get_following_users = (conn) -> {
            String query = "WITH following_ids AS (" +
                            "    SELECT following_id" +
                            "    FROM followers" +
                            "    WHERE user_id = ?" +
                            ")" +
                            "SELECT user_id AS following, username " +
                            "FROM users " +
                            "INNER JOIN following_ids " +
                            "ON user_id = following_id;";
            HashMap<Integer,String> results = new HashMap<>();
            try(PreparedStatement pst = conn.prepareStatement(query)){
                pst.setInt(1,user);
                ResultSet rs = pst.executeQuery();
                while(rs.next()){
                    int id = rs.getInt("following");
                    String username = rs.getString("username");
                    results.put(id, username);
                }
            }
            return results;
        };
        try{
            return dbUtils.doInTransaction(get_following_users);
        }catch(Exception e){
            logger.error("No following users for: "+user);
            throw new DatabaseException("No following users for: "+user,e);
        }
    }

    @Override
    public HashMap<Integer,String> getUsersToFollow(int user) throws DatabaseException {
        DbUtils.ThrowingFunction<Connection, HashMap<Integer,String>, Exception> get_users_to_follow = (conn) -> {
            String query = "SELECT user_id AS to_follow, username " +
                    "FROM users " +
                    "WHERE user_id != ? " +
                    "AND users.user_id NOT IN " +
                    "    (SELECT following_id " +
                    "     FROM followers " +
                    "     WHERE user_id = ?);";
            HashMap<Integer,String> results = new HashMap<>();
            try(PreparedStatement pst = conn.prepareStatement(query)){
                pst.setInt(1,user);
                pst.setInt(2,user);
                ResultSet rs = pst.executeQuery();
                while(rs.next()){
                    int to_follow_id = rs.getInt("to_follow");
                    String to_follow_username = rs.getString("username");
                    results.put(to_follow_id, to_follow_username);
                }
                return results;
            }
        };
        try{
            return dbUtils.doInTransaction(get_users_to_follow);
        }catch(Exception e){
            logger.error("No users to follow for user: "+user);
            throw new DatabaseException("No users to follow for user: "+user,e);
        }
    }
}
