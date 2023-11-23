package org.apostolis.posts;

import org.apostolis.AppConfig;
import org.apostolis.TestSuite;
import org.apostolis.common.DbUtils;
import org.apostolis.common.PageRequest;
import org.apostolis.posts.application.ports.in.*;
import org.apostolis.posts.domain.PostCreationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PostsTest {
    static DbUtils dbUtils;
    static CreatePostUseCase postService;
    static PostViewsUseCase postViewService;
    static ManageLinkUseCase linkService;
    private static final AppConfig appConfig = TestSuite.appConfig;

    @BeforeAll
    static void startDb(){
        TestSuite.initialDbSetup();
        dbUtils = appConfig.getDbUtils();
        postService = appConfig.getPostService();
        postViewService = appConfig.getPostViewsService();
        linkService = appConfig.getLinkService();

        DbUtils.ThrowingConsumer<Connection, Exception> setup_database = (connection) -> {
            try (PreparedStatement clean_stm = connection.prepareStatement(
                    "TRUNCATE TABLE comments, posts, followers RESTART IDENTITY CASCADE")) {
                clean_stm.executeUpdate();
            }
            // Populate users table
            String insert_users = "INSERT INTO users (username,password,role) VALUES" +
                    "('user1','pass','role')," +
                    "('user2','pass','role')," +
                    "('user3','pass','role')";
            try (PreparedStatement insert_users_stm = connection.prepareStatement(insert_users)) {
                insert_users_stm.executeUpdate();
            }
        };
        try {
            dbUtils.doInTransaction(setup_database);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void intermediateSetupDatabase(){
        DbUtils.ThrowingConsumer<Connection, Exception> intermediate_setup_database = (connection) -> {
            try (PreparedStatement clean_stm = connection.prepareStatement(
                    "TRUNCATE TABLE comments, posts, followers RESTART IDENTITY CASCADE")) {
                clean_stm.executeUpdate();
            }
            // Populate posts table (post_id is autoincrement 1,2,3,4 accordingly)
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
            String insert_comments = "INSERT INTO comments (post_id,user_id,text,created) VALUES" +
                    "(1,2,'com1 from user2',?)," +
                    "(2,1,'com1 from user1',?)," +
                    "(3,1,'com1 from user1',?)," +
                    "(1,3,'com2 from user3',?),"+
                    "(2,2,'com2 from user2',?),"+
                    "(2,3,'com3 from user3',?)";
            try(PreparedStatement insert_comments_stm = connection.prepareStatement(insert_comments)){
                for(int i=1; i<=6; i++){
                    insert_comments_stm.setTimestamp(i,
                            Timestamp.valueOf(LocalDateTime.now(AppConfig.clock).plusSeconds(120 + i*30)));
                }
                insert_comments_stm.executeUpdate();
            }

            // Populate followers table
            String insert_follows = "INSERT INTO followers VALUES(2,1),(3,1),(1,2),(2,3)";
            try(PreparedStatement insert_follows_stm = connection.prepareStatement(insert_follows)){
                insert_follows_stm.executeUpdate();
            }
        };
        try {
            dbUtils.doInTransaction(intermediate_setup_database);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void postFromFreeUserUnderLimit(){
        CreatePostCommand createPostCommand = new CreatePostCommand(1,"pass_length_post","FREE"); // 16
        assertDoesNotThrow(() -> postService.createPost(createPostCommand));
    }

    @Test
    void postFromFreeUserExceedLimit(){
        CreatePostCommand createPostCommand = new CreatePostCommand(1,
                "pass_length_post_exceeding_the_limits","FREE"); // 37
        assertThrows(PostCreationException.class, () -> postService.createPost(createPostCommand));
    }

    @Test
    void postFromPremiumUserBetweenLimits(){
        CreatePostCommand createPostCommand = new CreatePostCommand(2,
                "post_length_greater_than_free","PREMIUM"); // 29
        assertDoesNotThrow(() -> postService.createPost(createPostCommand));
    }

    @Test
    void postFromPremiumUserExceedLimit(){
        CreatePostCommand createPostCommand = new CreatePostCommand(2,
                "pass_length_post_exceeding_the_limits","PREMIUM"); // 37
        assertThrows(PostCreationException.class, () -> postService.createPost(createPostCommand));
    }

    @Test
    void getFollowingPosts(){
        PostViewsQuery postViewsQuery = new PostViewsQuery(2, new PageRequest(0,Integer.MAX_VALUE));
        Map<Long, List<Object>> result = postViewService.getFollowingPosts(postViewsQuery);
        HashMap<Integer, String> posts_from_first_follower = (HashMap)result.get(1).get(1);
        HashMap<Integer, String> posts_from_second_follower = (HashMap)result.get(3).get(1);

        assertEquals(2, result.keySet().size());
        assertEquals(2, posts_from_first_follower.size());
        assertEquals(1, posts_from_second_follower.size());
        assertEquals("post2 from user1",posts_from_first_follower.values().toArray()[0]);
        assertEquals("post1 from user1",posts_from_first_follower.values().toArray()[1]);
        assertEquals("post1 from user3",posts_from_second_follower.values().toArray()[0]);
    }

    @Test
    void getOwnPostsWithNLatestComments(){
        OwnPostsWithNCommentsQuery viewQuery = new OwnPostsWithNCommentsQuery(1,2,new PageRequest(0,Integer.MAX_VALUE));
        Map<Long, List<Object>> results = postViewService.getOwnPostsWithNLatestComments(viewQuery);

        HashMap<Integer, String> post1_comments = (HashMap)results.get(1).get(1);
        HashMap<Integer, String> post2_comments = (HashMap)results.get(2).get(1);

        assertEquals(2,results.keySet().size());
        assertEquals(2,post1_comments.size());
        assertEquals(2,post2_comments.size());

        assertEquals("com2 from user3", post1_comments.values().toArray()[0]);
        assertEquals("com1 from user2", post1_comments.values().toArray()[1]);
        assertEquals("com3 from user3", post2_comments.values().toArray()[0]);
        assertEquals("com2 from user2", post2_comments.values().toArray()[1]);
    }

    @Test
    void LinkForPostAndComments(){
        String url = linkService.createLink(new CreateLinkCommand(1,1));
        List<Object> decoded = linkService.decodeLink(url);
        HashMap<Integer, String> post_comments = (HashMap)decoded.get(1);
        assertEquals("post1 from user1",decoded.get(0));
        assertEquals(2,post_comments.size());
    }
}
