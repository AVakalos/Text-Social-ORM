package org.apostolis.comments;

import org.apostolis.AppConfig;
import org.apostolis.TestSuite;
import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.comments.application.ports.in.CreateCommentCommand;
import org.apostolis.comments.application.ports.in.CreateCommentUseCase;
import org.apostolis.comments.application.ports.in.ViewCommentsQuery;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.common.DbUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CommentsTest {
    static DbUtils dbUtils;
    static CreateCommentUseCase commentService;
    static CommentsViewsUseCase commentsViewsService;
    private static final AppConfig appConfig = TestSuite.appConfig;

    @BeforeAll
    static void startDb(){
        TestSuite.initialDbSetup();
        dbUtils = appConfig.getDbUtils();
        commentService = appConfig.getCommentService();
        commentsViewsService = appConfig.getCommentsViewService();

        DbUtils.ThrowingConsumer<Connection, Exception> setup_database = (connection) -> {
            try (PreparedStatement clean_stm = connection.prepareStatement(
                    "TRUNCATE TABLE users, posts, followers RESTART IDENTITY CASCADE")) {
                clean_stm.executeUpdate();
            }
            String insert_users = "INSERT INTO users (username,password,role) VALUES" +
                    "('user1','pass','FREE')," +
                    "('user2','pass','PREMIUM')," +
                    "('user3','pass','FREE')";
            try (PreparedStatement insert_users_stm = connection.prepareStatement(insert_users)) {
                insert_users_stm.executeUpdate();
            }
            String insert_posts = "INSERT INTO posts (user_id, text, created) VALUES " +
                    "(1,'post1 from user1',?)," +
                    "(1,'post2 from user1',?)," +
                    "(2,'post1 from user2',?)," +
                    "(3,'post1 from user3',?)";
            try (PreparedStatement insert_posts_stm = connection.prepareStatement(insert_posts)) {
                for (int i = 1; i <= 4; i++) {
                    insert_posts_stm.setTimestamp(i, Timestamp.valueOf(
                            LocalDateTime.now(AppConfig.clock).plusSeconds(i * 30)));
                }
                insert_posts_stm.executeUpdate();
            }
            // Populate followers table
            String insert_follows = "INSERT INTO followers VALUES(2,1),(3,1),(1,2),(2,3)";
            try(PreparedStatement insert_follows_stm = connection.prepareStatement(insert_follows)){
                insert_follows_stm.executeUpdate();
            }
        };
        try {
            dbUtils.doInTransaction(setup_database);
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @BeforeEach
    void intermediateSetupDatabase(){
        DbUtils.ThrowingConsumer<Connection, Exception> intermediate_setup_database = (connection) -> {
            try (PreparedStatement clean_stm = connection.prepareStatement(
                    "TRUNCATE TABLE comments RESTART IDENTITY CASCADE")) {
                clean_stm.executeUpdate();
            }

            String insert_comments = "INSERT INTO comments (post_id,user_id,text,created) VALUES" +
                    "(1,2,'com1 from user2',?)," +
                    "(2,1,'com1 from user1',?)," +
                    "(3,1,'com1 from user1',?)," +
                    "(1,3,'com2 from user3',?),"+
                    "(2,2,'com2 from user2',?),"+
                    "(2,3,'com3 from user3',?)";
            try(PreparedStatement insert_comments_stm = connection.prepareStatement(insert_comments)){
                for(int i=1; i<=6; i++){
                    insert_comments_stm.setTimestamp(i, Timestamp.valueOf(
                            LocalDateTime.now(AppConfig.clock).plusSeconds(120 + i*30)));
                }
                insert_comments_stm.executeUpdate();
            }
        };
        try {
            dbUtils.doInTransaction(intermediate_setup_database);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void commentFromFreeUser(){
        CreateCommentCommand createCommentCommand = new CreateCommentCommand(1,1,"first comment","FREE");
        assertDoesNotThrow(()->commentService.createComment(createCommentCommand));
    }

    @Test
    void commentFromFreeUserExceedLimit(){
        CreateCommentCommand createCommentCommand1 = new CreateCommentCommand(1,1,"comment1","FREE");
        CreateCommentCommand createCommentCommand2 = new CreateCommentCommand(1,1,"comment2","FREE");
        CreateCommentCommand createCommentCommand3 = new CreateCommentCommand(1,1,"comment3","FREE");

        assertThrows(CommentCreationException.class, () -> {
            commentService.createComment(createCommentCommand1);
            commentService.createComment(createCommentCommand2);
            commentService.createComment(createCommentCommand3);
        });
    }

    @Test
    void commentFromPremiumUser(){
        CreateCommentCommand createCommentCommand1 = new CreateCommentCommand(2,1,"comment1","PREMIUM");
        CreateCommentCommand createCommentCommand2 = new CreateCommentCommand(2,1,"comment2","PREMIUM");
        CreateCommentCommand createCommentCommand3 = new CreateCommentCommand(2,1,"comment3","PREMIUM");

        assertDoesNotThrow(() -> {
            commentService.createComment(createCommentCommand1);
            commentService.createComment(createCommentCommand2);
            commentService.createComment(createCommentCommand3);
        });
    }

    @Test
    void getAllCommentsOnOwnPosts(){
        ViewCommentsQuery viewCommentsQuery = new ViewCommentsQuery(1,0,Integer.MAX_VALUE);
        Map<Integer, List<Object>> result = commentsViewsService.getCommentsOnOwnPosts(viewCommentsQuery);

        HashMap<Integer, String> post1_comments = (HashMap)result.get(2).get(1);
        HashMap<Integer, String> post2_comments = (HashMap)result.get(1).get(1);

        assertEquals(2,result.size());
        assertEquals(5,post1_comments.size()+post2_comments.size());
    }

    @Test
    void getLatestCommentsOnOwnOrFollowersPosts(){
        ViewCommentsQuery viewCommentsQuery = new ViewCommentsQuery(2,0,Integer.MAX_VALUE);
        Map<Integer, HashMap<Integer,List<Object>>> result =
                commentsViewsService.getLatestCommentsOnOwnOrFollowingPosts(viewCommentsQuery);
        assertEquals(4,result.get(1).size() + result.get(3).size() + result.get(2).size());
    }
}
