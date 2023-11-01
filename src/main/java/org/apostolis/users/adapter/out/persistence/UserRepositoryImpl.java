package org.apostolis.users.adapter.out.persistence;

import org.apostolis.common.DbUtils;
import org.apostolis.exception.DatabaseException;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.users.application.ports.in.RegisterCommand;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserRepositoryImpl implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);
    private final DbUtils dbUtils;

    public UserRepositoryImpl(DbUtils dbUtils){
        this.dbUtils = dbUtils;
    }

    @Override
    public void save(User user, PasswordEncoder passwordEncoder) {
        DbUtils.ThrowingConsumer<Connection,Exception> insertUserIntoDb = (conn) -> {
            try(PreparedStatement insert_stm = conn.prepareStatement(
                    "INSERT INTO Users (username,password,role) VALUES(?,?,?)")){
                insert_stm.setString(1, user.username());
                insert_stm.setString(2, passwordEncoder.encodePassword(user.password()));
                insert_stm.setString(3, user.role());
                insert_stm.executeUpdate();
            }
        };
        try{
            dbUtils.doInTransaction(insertUserIntoDb);
            logger.info("User saved successfully in the database.");
        }catch (Exception e){
            logger.error("User didn't saved.");
            throw new DatabaseException("User didn't saved",e);
        }
    }

    @Override
    public User getByUsername(String username) {

        DbUtils.ThrowingFunction<Connection, User, Exception> retrieveUser = (conn) -> {
            try(PreparedStatement retrieve_stm = conn.prepareStatement(
                    "SELECT username,password,role FROM Users WHERE username=?",
                    ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE)){

                retrieve_stm.setString(1,username);
                ResultSet rs = retrieve_stm.executeQuery();
                User user = null;
                if(rs.next()){
                    logger.info("User: "+username+" found in the database");
                    rs.beforeFirst();
                    while(rs.next()){
                        user = new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("role")
                        );
                    }
                }else{
                    return null;
                }
                return user;
            }
        };
        try {
            return dbUtils.doInTransaction(retrieveUser);
        }catch(Exception e){
            logger.error("User could not be retrieved");
            throw new DatabaseException("User could not be retrieved",e);
        }
    }

    @Override
    public int getUserIdFromUsername(String username) {
        DbUtils.ThrowingFunction<Connection,Integer, Exception> get_user_id = (conn) -> {
            int id;
            try(PreparedStatement pst = conn.prepareStatement("SELECT user_id FROM users WHERE username=?")){
                pst.setString(1, username);
                ResultSet rs = pst.executeQuery();
                rs.next();
                id = rs.getInt("user_id");
            }
            return id;
        };
        try{
            return dbUtils.doInTransaction(get_user_id);
        }catch(Exception e){
            logger.error("Could not retrieve the id from username: "+username);
            throw new DatabaseException("Could not retrieve the id from username: "+username,e);
        }
    }

    @Override
    public String getUsernameFromId(int userId) {
        DbUtils.ThrowingFunction<Connection, String, Exception> get_username = (conn) -> {
            String username;
            try(PreparedStatement pst = conn.prepareStatement("SELECT username FROM users WHERE user_id=?")){
                pst.setInt(1, userId);
                ResultSet rs = pst.executeQuery();
                rs.next();
                username = rs.getString("username");
            }
            return username;
        };
        try{
            return dbUtils.doInTransaction(get_username);
        }catch(Exception e){
            logger.error("Could not retrieve the username with id: "+userId);
            throw new DatabaseException("Could not retrieve the username with id: "+userId,e);
        }
    }
}
