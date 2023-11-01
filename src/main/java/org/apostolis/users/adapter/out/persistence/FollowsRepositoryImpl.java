package org.apostolis.users.adapter.out.persistence;

import org.apostolis.App;
import org.apostolis.common.DbUtils;
import org.apostolis.exception.DatabaseException;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class FollowsRepositoryImpl implements FollowsRepository {

    private static final Logger logger = LoggerFactory.getLogger(FollowsRepositoryImpl.class);
    private final DbUtils dbUtils;

    public FollowsRepositoryImpl(DbUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public void saveFollow(int user) throws IllegalArgumentException, DatabaseException{
        DbUtils.ThrowingConsumer<Connection,Exception> saveFollowIntoDb = (conn) -> {
            try(PreparedStatement savefollow_stm = conn.prepareStatement("INSERT INTO followers VALUES (?,?)")){
                savefollow_stm.setInt(1, App.currentUserId.get());
                savefollow_stm.setInt(2, user);
                savefollow_stm.executeUpdate();
            }
        };
        try{
            dbUtils.doInTransaction(saveFollowIntoDb);
            logger.info("Follow saved successfully in the database.");
        }catch (Exception e){
            logger.error("Follow didn't saved.");
            throw new DatabaseException("You already follow this user or the user does not exist",e);
        }
    }

    @Override
    public void deleteFollow(int user) throws IllegalArgumentException, DatabaseException {
        DbUtils.ThrowingConsumer<Connection,Exception> deleteFollowerFromDb = (conn) -> {
            try(PreparedStatement delete_follower_stm = conn.prepareStatement(
                    "DELETE FROM followers WHERE user_id = ? AND follower_id=?")){
                delete_follower_stm.setInt(1,App.currentUserId.get());
                delete_follower_stm.setInt(2,user);
                int count = delete_follower_stm.executeUpdate();
                if (count == 0){
                    logger.info("User didnt found");
                    throw new IllegalArgumentException("You were not following this user or user does not exist");
                }
            }
        };
        try{
            dbUtils.doInTransaction(deleteFollowerFromDb);
            logger.info("Follow deleted successfully from database.");
        }catch (Exception e){
            logger.error("Follow didn't deleted.");
            logger.error(e.getMessage());
            if (e instanceof IllegalArgumentException){
                throw new DatabaseException(e.getMessage());
            }else {
                throw new DatabaseException("Follow didn't deleted", e);
            }
        }
    }

    @Override
    public ArrayList<String> getFollowers(int user) throws DatabaseException {
        DbUtils.ThrowingFunction<Connection, ArrayList<String>, Exception> get_followers = (conn) -> {
            String query = "WITH follower_ids AS (SELECT follower_id " +
                    "   FROM followers " +
                    "   WHERE user_id = ?) " +
                    "SELECT username AS followers " +
                    "FROM users " +
                    "INNER JOIN follower_ids AS f " +
                    "ON user_id = f.follower_id;";
            ArrayList<String> results = new ArrayList<>();
            try(PreparedStatement pst = conn.prepareStatement(query)){
                pst.setInt(1,user);
                ResultSet rs = pst.executeQuery();
                while(rs.next()){
                    String comment = rs.getString("followers");
                    if(comment != null){
                        results.add(comment);
                    }
                }
                return results;
            }
        };
        try{
            return dbUtils.doInTransaction(get_followers);
        }catch(Exception e){
            logger.error("No followers for user: "+user);
            throw new DatabaseException("No followers for user: "+user,e);
        }
    }

    @Override
    public ArrayList<String> getUsersToFollow(int user) throws DatabaseException {
        DbUtils.ThrowingFunction<Connection, ArrayList<String>, Exception> get_users_to_follow = (conn) -> {
            String query = "SELECT username AS to_follow " +
                    "FROM users " +
                    "WHERE user_id != ? " +
                    "AND users.user_id NOT IN " +
                    "    (SELECT follower_id " +
                    "     FROM followers " +
                    "     WHERE user_id = ?);";
            ArrayList<String> results = new ArrayList<>();
            try(PreparedStatement pst = conn.prepareStatement(query)){
                pst.setInt(1,user);
                pst.setInt(2,user);
                ResultSet rs = pst.executeQuery();
                while(rs.next()){
                    String comment = rs.getString("to_follow");
                    if(comment != null){
                        results.add(comment);
                    }
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
