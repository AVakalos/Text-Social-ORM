package org.apostolis.users.adapter.out.persistence;

import org.apostolis.App;
import org.apostolis.common.DbUtils;
import org.apostolis.users.application.ports.in.FollowCommand;
import org.apostolis.users.application.ports.out.FollowingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class FollowingRepositoryImpl implements FollowingRepository {

    private static final Logger logger = LoggerFactory.getLogger(FollowingRepositoryImpl.class);
    private final DbUtils dbUtils;

    public FollowingRepositoryImpl(DbUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public void saveFollow(FollowCommand followCommand) throws Exception{
        DbUtils.ThrowingConsumer<Connection,Exception> saveFollowIntoDb = (conn) -> {
            try(PreparedStatement savefollow_stm = conn.prepareStatement("INSERT INTO followers VALUES (?,?)")){
                savefollow_stm.setInt(1, App.currentUserId.get());
                savefollow_stm.setInt(2,followCommand.follows());
                savefollow_stm.executeUpdate();
            }
        };
        try{
            dbUtils.doInTransaction(saveFollowIntoDb);
            logger.info("Follow saved successfully in the database.");
        }catch (Exception e){
            logger.error("Follow didn't saved.");
            throw new IllegalArgumentException("You already follow this user or the user does not exist.");
        }
    }
}
