package org.apostolis.posts;

import org.apostolis.AppConfig;
import org.apostolis.TestSuite;
import org.apostolis.comments.adapter.out.persistence.CommentId;
import org.apostolis.comments.domain.CommentDTO;
import org.apostolis.common.PageRequest;
import org.apostolis.posts.adapter.out.persistence.PostId;
import org.apostolis.posts.application.ports.in.*;
import org.apostolis.posts.domain.PostCreationException;
import org.apostolis.posts.domain.PostDTO;
import org.apostolis.users.adapter.out.persistence.UserId;
import org.hibernate.SessionFactory;
import org.hibernate.query.MutationQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PostsTest {
    static SessionFactory sessionFactory;
    static CreatePostUseCase postService;
    static PostViewsUseCase postViewService;
    static ManageLinkUseCase linkService;
    private static final AppConfig appConfig = TestSuite.appConfig;

    @BeforeAll
    static void startDb(){
        TestSuite.initialDbSetup();
        sessionFactory = TestSuite.getSessionFactory();
        postService = appConfig.getPostService();
        postViewService = appConfig.getPostViewsService();
        linkService = appConfig.getLinkService();

        try {
            sessionFactory.inTransaction(session -> {
                String truncate_tables = "TRUNCATE TABLE comments, posts, followers RESTART IDENTITY CASCADE";
                session.createNativeMutationQuery(truncate_tables).executeUpdate();

                String insert_users = """
                        INSERT INTO users (username,password,role) VALUES
                        ('user1','pass','role'),
                        ('user2','pass','role'),
                        ('user3','pass','role')""";
                session.createNativeMutationQuery(insert_users).executeUpdate();

            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void intermediateSetupDatabase(){
        try {
            sessionFactory.inTransaction(session -> {
                String truncate_tables = "TRUNCATE TABLE comments, posts, followers RESTART IDENTITY CASCADE";
                session.createNativeMutationQuery(truncate_tables).executeUpdate();

                // Populate posts table (post_id is autoincrement 1,2,3,4 accordingly)
                String insert_posts = """
                        INSERT INTO posts (user_id, text, isshared ,createdat) VALUES
                        (1,'post1 from user1',false,?),
                        (1,'post2 from user1',false,?),
                        (2,'post1 from user2',false,?),
                        (3,'post1 from user3',false,?)""";

                MutationQuery posts_query = session.createNativeMutationQuery(insert_posts);
                for (int i = 1; i <= 4; i++) {
                    posts_query.setParameter(i, Timestamp.valueOf(
                            LocalDateTime.now(AppConfig.getClock()).plusSeconds(i * 30)));
                }
                posts_query.executeUpdate();

                String insert_comments = """
                        INSERT INTO comments (post_id,user_id,text,createdat) VALUES
                        (1,2,'com1 from user2',?),
                        (2,1,'com1 from user1',?),
                        (3,1,'com1 from user1',?),
                        (1,3,'com2 from user3',?),
                        (2,2,'com2 from user2',?),
                        (2,3,'com3 from user3',?)""";

                MutationQuery comments_query = session.createNativeMutationQuery(insert_comments);
                for(int i=1; i<=6; i++){
                    comments_query.setParameter(i,
                            Timestamp.valueOf(LocalDateTime.now(AppConfig.getClock()).plusSeconds(120 + i*30)));
                }
                comments_query.executeUpdate();

                // Populate followers table
                String insert_follows = "INSERT INTO followers VALUES(2,1),(3,1),(1,2),(2,3)";
                session.createNativeMutationQuery(insert_follows).executeUpdate();
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void postFromFreeUserUnderLimit(){
        CreatePostCommand createPostCommand = new CreatePostCommand(1L,"pass_length_post","FREE"); // 16
        assertDoesNotThrow(() -> postService.createPost(createPostCommand));
    }

    @Test
    void postFromFreeUserExceedLimit(){
        CreatePostCommand createPostCommand = new CreatePostCommand(
                1L, "pass_length_post_exceeding_the_limits","FREE"); // 37
        assertThrows(PostCreationException.class, () -> postService.createPost(createPostCommand));
    }

    @Test
    void postFromPremiumUserBetweenLimits(){
        CreatePostCommand createPostCommand = new CreatePostCommand(
                2L, "post_length_greater_than_free","PREMIUM"); // 29
        assertDoesNotThrow(() -> postService.createPost(createPostCommand));
    }

    @Test
    void postFromPremiumUserExceedLimit(){
        CreatePostCommand createPostCommand = new CreatePostCommand(
                2L, "pass_length_post_exceeding_the_limits","PREMIUM"); // 37
        assertThrows(PostCreationException.class, () -> postService.createPost(createPostCommand));
    }

    @Test
    void getFollowingPosts(){
        PostViewsQuery postViewsQuery = new PostViewsQuery(new UserId(2L), new PageRequest(0,Integer.MAX_VALUE));
        Map<UserId, List<Object>> result = postViewService.getFollowingPosts(postViewsQuery);

        System.out.println(result.keySet());

        Map<PostId, PostDTO> posts_from_first_follower = (HashMap)result.get(new UserId(1L)).get(1);
        Map<PostId, PostDTO> posts_from_second_follower = (HashMap)result.get(new UserId(3L)).get(1);

        PostDTO p1 = (PostDTO) posts_from_first_follower.values().toArray()[0];
        PostDTO p2 = (PostDTO) posts_from_first_follower.values().toArray()[1];
        PostDTO p3 = (PostDTO) posts_from_second_follower.values().toArray()[0];
        assertEquals(2, result.keySet().size());
        assertEquals(2, posts_from_first_follower.size());
        assertEquals(1, posts_from_second_follower.size());
        assertEquals("post2 from user1",p1.text());
        assertEquals("post1 from user1",p2.text());
        assertEquals("post1 from user3",p3.text());
    }

    @Test
    void getOwnPostsWithNLatestComments(){
        OwnPostsWithNCommentsQuery viewQuery = new OwnPostsWithNCommentsQuery(1L,2,new PageRequest(0,Integer.MAX_VALUE));
        Map<PostId, List<Object>> results = postViewService.getOwnPostsWithNLatestComments(viewQuery);

        HashMap<CommentId, String> post1_comments = (HashMap)results.get(new PostId(1L)).get(1);
        HashMap<CommentId, String> post2_comments = (HashMap)results.get(new PostId(2L)).get(1);

        assertEquals(2,results.keySet().size());
        assertEquals(2,post1_comments.size());
        assertEquals(3,post2_comments.size());

        CommentDTO c1 = (CommentDTO) post1_comments.values().toArray()[0];
        CommentDTO c2 = (CommentDTO) post1_comments.values().toArray()[1];
        CommentDTO c3 = (CommentDTO) post2_comments.values().toArray()[0];
        CommentDTO c4 = (CommentDTO) post2_comments.values().toArray()[1];

        assertEquals("com2 from user3",c1.text());
        assertEquals("com1 from user2", c2.text());
        assertEquals("com3 from user3", c3.text());
        assertEquals("com2 from user2", c4.text());
    }

    @Test
    void LinkForPostAndComments(){
        String url = linkService.createLink(new CreateLinkCommand(1L,1L));
        List<Object> decoded = linkService.decodeLink(url);
        Map<CommentId, String> post_comments = (HashMap)decoded.get(1);
        assertEquals("post1 from user1",decoded.get(0));
        assertEquals(2,post_comments.size());
    }
}
