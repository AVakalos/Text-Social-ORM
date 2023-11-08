package org.apostolis.users;

import org.apostolis.AppConfig;
import org.apostolis.TestSuite;
import org.apostolis.common.DbUtils;
import org.apostolis.users.application.ports.in.FollowsCommand;
import org.apostolis.users.application.ports.in.FollowsUseCase;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FollowsTest {
    static DbUtils dbUtils;
    static FollowsUseCase followsService;
    static GetFollowersAndUsersToFollowUseCase getFollowsService;
    private static final AppConfig appConfig = TestSuite.appConfig;

    @BeforeAll
    static void setupDb(){
        TestSuite.initialDbSetup();
        dbUtils = appConfig.getDbUtils();
        followsService = appConfig.getFollowsService();
        getFollowsService = appConfig.getGetFollowsService();

        DbUtils.ThrowingConsumer<Connection, Exception> setup_database = (connection) -> {
            try(PreparedStatement clean_stm = connection.prepareStatement(
                    "TRUNCATE TABLE users,comments, posts, followers RESTART IDENTITY CASCADE")){
                clean_stm.executeUpdate();
            }
            // register users
            String insert_user1 = "INSERT INTO users (username,password,role) VALUES('1','pass','FREE')";
            String insert_user_2 = "INSERT INTO users (username,password,role) VALUES('2','pass','PREMIUM')";
            String insert_user_3 = "INSERT INTO users (username,password,role) VALUES('3','pass','PREMIUM')";

            try(PreparedStatement insert_user_1_stm = connection.prepareStatement(insert_user1);
                PreparedStatement insert_user_2_stm = connection.prepareStatement(insert_user_2);
                PreparedStatement insert_user_3_stm = connection.prepareStatement(insert_user_3)){
                insert_user_1_stm.executeUpdate();
                insert_user_2_stm.executeUpdate();
                insert_user_3_stm.executeUpdate();
            }
        };
        try {
            // initial clean for any possible garbage data
            dbUtils.doInTransaction(setup_database);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void intermediateSetupDatabase() {
        DbUtils.ThrowingConsumer<Connection, Exception> intermediate_setup_database = (connection) -> {
            String clean_query = "TRUNCATE TABLE followers RESTART IDENTITY CASCADE";
            String insert_query = "INSERT INTO followers VALUES(2,1),(3,1),(1,2),(2,3)";
            try(PreparedStatement clean_followers = connection.prepareStatement(clean_query);
                PreparedStatement add_follow_stm = connection.prepareStatement(insert_query)){
                clean_followers.executeUpdate();
                add_follow_stm.executeUpdate();
            }
        };
        try{
            dbUtils.doInTransaction(intermediate_setup_database);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void follow(){
        assertDoesNotThrow(() -> followsService.followUser(new FollowsCommand(1,3)));
    }

    @Test
    void followYourselfNotAllowed(){
        assertThrows(IllegalArgumentException.class,
                () -> followsService.followUser(new FollowsCommand(1,1)));
    }

    @Test
    void unfollow(){
        assertDoesNotThrow(() -> followsService.unfollowUser(new FollowsCommand(1,2)));
    }

    @Test
    void getFollowers(){
        HashMap<Integer,String> results = getFollowsService.getFollowers(1);
        assertEquals(2,results.size());
    }

    @Test
    void getUsersToFollowAvailable(){
        HashMap<Integer,String> results = getFollowsService.getUsersToFollow(3);
        assertEquals(1,results.size());
    }

    @Test
    void getUsersToFollowNoAvailable(){
        HashMap<Integer,String> results = getFollowsService.getUsersToFollow(2);
        assertEquals(0,results.size());
    }
}
