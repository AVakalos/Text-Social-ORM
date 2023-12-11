package org.apostolis.users;

import org.apostolis.AppConfig;
import org.apostolis.TestSuite;
import org.apostolis.common.TransactionUtils;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.application.ports.in.FollowsCommand;
import org.apostolis.users.application.ports.in.FollowsUseCase;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.apostolis.users.domain.UserDTO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FollowsTest {
    static TransactionUtils transactionUtils;
    static FollowsUseCase followsService;
    static GetFollowersAndUsersToFollowUseCase getFollowsService;
    private static final AppConfig appConfig = TestSuite.appConfig;

    @BeforeAll
    static void setupDb(){
        TestSuite.initialDbSetup();
        transactionUtils = appConfig.getTransactionUtils();
        followsService = appConfig.getFollowsService();
        getFollowsService = appConfig.getGetFollowsService();

        TransactionUtils.ThrowingConsumer<Session,Exception> task = (session) -> {
            String truncate_tables = "TRUNCATE TABLE users,comments, posts, followers RESTART IDENTITY CASCADE";
            session.createNativeMutationQuery(truncate_tables).executeUpdate();
            // register users
            String insert_user1 = "INSERT INTO users (username,password,role) VALUES('1','pass','FREE')";
            String insert_user_2 = "INSERT INTO users (username,password,role) VALUES('2','pass','PREMIUM')";
            String insert_user_3 = "INSERT INTO users (username,password,role) VALUES('3','pass','PREMIUM')";
            session.createNativeMutationQuery(insert_user1).executeUpdate();
            session.createNativeMutationQuery(insert_user_2).executeUpdate();
            session.createNativeMutationQuery(insert_user_3).executeUpdate();
        };
        try {
            transactionUtils.doInTransaction(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void intermediateSetupDatabase() {
        TransactionUtils.ThrowingConsumer<Session,Exception> task = (session) -> {
            String clean_query = "TRUNCATE TABLE followers RESTART IDENTITY CASCADE";
            String insert_query = "INSERT INTO followers VALUES(2,1),(3,1),(1,2),(2,3)";
            session.createNativeMutationQuery(clean_query).executeUpdate();
            session.createNativeMutationQuery(insert_query).executeUpdate();
        };
        try{
            transactionUtils.doInTransaction(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void follow(){
        assertDoesNotThrow(() -> followsService.followUser(new FollowsCommand(new UserId(1L),new UserId(3L))));
    }

    @Test
    void followYourselfNotAllowed(){
        assertThrows(IllegalArgumentException.class,
                () -> followsService.followUser(new FollowsCommand(new UserId(1L),new UserId(1L))));
    }

    @Test
    void unfollow(){
        assertDoesNotThrow(() -> followsService.unfollowUser(new FollowsCommand(new UserId(1L),new UserId(2L))));
    }

    @Test
    void getFollowers() throws Exception {
        Map<UserId, UserDTO> results = getFollowsService.getFollowers(
                new UserId(1L),0,Integer.MAX_VALUE);
        assertEquals(2,results.size());
    }

    @Test
    void getUsersToFollowAvailable() throws Exception {
        Map<UserId, UserDTO> results = getFollowsService.getUsersToFollow(
                new UserId(3L),0,Integer.MAX_VALUE);
        assertEquals(1,results.size());
    }

    @Test
    void getUsersToFollowNoAvailable() throws Exception {
        Map<UserId, UserDTO> results = getFollowsService.getUsersToFollow(
                new UserId(2L),0,Integer.MAX_VALUE);
        System.out.println(results);
        assertEquals(0,results.size());
    }
}
