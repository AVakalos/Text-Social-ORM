package org.apostolis.posts;

import org.apostolis.AppConfig;
import org.apostolis.TestSuite;
import org.apostolis.comments.domain.CommentId;
import org.apostolis.comments.domain.CommentDetails;
import org.apostolis.common.PageRequest;
import org.apostolis.common.TransactionUtils;
import org.apostolis.posts.domain.*;
import org.apostolis.posts.application.ports.in.*;
import org.apostolis.users.domain.UserId;
import org.hibernate.Session;
import org.hibernate.query.MutationQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class PostsTest {
    static TransactionUtils transactionUtils;
    static CreatePostUseCase postService;
    static PostViewsUseCase postViewService;
    static ManageLinkUseCase linkService;
    private static final AppConfig appConfig = TestSuite.appConfig;

    @BeforeAll
    static void startDb(){
        TestSuite.initialDbSetup();
        transactionUtils = appConfig.getTransactionUtils();
        postService = appConfig.getPostService();
        postViewService = appConfig.getPostViewsService();
        linkService = appConfig.getLinkService();

        TransactionUtils.ThrowingConsumer<Session,Exception> task = (session) -> {
            String truncate_tables = "TRUNCATE TABLE comments, posts, followers RESTART IDENTITY CASCADE";
            session.createNativeMutationQuery(truncate_tables).executeUpdate();

            String insert_users = """
                        INSERT INTO users (username,password,role) VALUES
                        ('user1','pass','role'),
                        ('user2','pass','role'),
                        ('user3','pass','role')""";
            session.createNativeMutationQuery(insert_users).executeUpdate();
        };
        try {
            transactionUtils.doInTransaction(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void intermediateSetupDatabase(){
        TransactionUtils.ThrowingConsumer<Session,Exception> task = (session) -> {
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
        };
        try {
            transactionUtils.doInTransaction(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void postFromFreeUserUnderLimit(){
        CreatePostCommand createPostCommand = new CreatePostCommand(new UserId(1L),"pass_length_post","FREE"); // 16
        assertDoesNotThrow(() -> postService.createPost(createPostCommand));
    }

    @Test
    void postFromFreeUserExceedLimit(){
        CreatePostCommand createPostCommand = new CreatePostCommand(
                new UserId(1L), "pass_length_post_exceeding_the_limits","FREE"); // 37
        assertThrows(PostCreationException.class, () -> postService.createPost(createPostCommand));
    }

    @Test
    void postFromPremiumUserBetweenLimits(){
        CreatePostCommand createPostCommand = new CreatePostCommand(
                new UserId(2L), "post_length_greater_than_free","PREMIUM"); // 29
        assertDoesNotThrow(() -> postService.createPost(createPostCommand));
    }

    @Test
    void postFromPremiumUserExceedLimit(){
        CreatePostCommand createPostCommand = new CreatePostCommand(
                new UserId(2L), "pass_length_post_exceeding_the_limits","PREMIUM"); // 37
        assertThrows(PostCreationException.class, () -> postService.createPost(createPostCommand));
    }

    @Test
    void getFollowingPosts() throws Exception {
        PostViewsQuery postViewsQuery = new PostViewsQuery(new UserId(2L), new PageRequest(0,Integer.MAX_VALUE));
        FollowingPostsView followingPosts = postViewService.getFollowingPosts(postViewsQuery);

        Map<PostId, PostDetails> posts_from_first_follower = followingPosts.getFollowingPosts().getData().get(new UserId(1L));
        Map<PostId, PostDetails> posts_from_second_follower = followingPosts.getFollowingPosts().getData().get(new UserId(3L));

        PostDetails p1 = (PostDetails) posts_from_first_follower.values().toArray()[0];
        PostDetails p2 = (PostDetails) posts_from_first_follower.values().toArray()[1];
        PostDetails p3 = (PostDetails) posts_from_second_follower.values().toArray()[0];
        assertEquals(2, followingPosts.getFollowingPosts().getData().keySet().size());
        assertEquals(2, posts_from_first_follower.size());
        assertEquals(1, posts_from_second_follower.size());
        assertEquals("post2 from user1",p1.text());
        assertEquals("post1 from user1",p2.text());
        assertEquals("post1 from user3",p3.text());
    }

    @Test
    void getOwnPostsWithNLatestComments() throws Exception {
        OwnPostsWithNCommentsQuery viewQuery =
                new OwnPostsWithNCommentsQuery(1L,100,new PageRequest(0,Integer.MAX_VALUE));
        PostsWithNLatestCommentsView postsWithNLatestComments = postViewService.getOwnPostsWithNLatestComments(viewQuery);

        Map<CommentId, CommentDetails> post1_comments = postsWithNLatestComments.getCommentsPerPost().getData().get(new PostId(1L));
        Map<CommentId, CommentDetails> post2_comments = postsWithNLatestComments.getCommentsPerPost().getData().get(new PostId(2L));


        assertEquals(2,postsWithNLatestComments.getCommentsPerPost().getData().keySet().size());
        assertEquals(2,post1_comments.size());
        assertEquals(3,post2_comments.size());

        CommentDetails c1 = (CommentDetails) post1_comments.values().toArray()[0];
        CommentDetails c2 = (CommentDetails) post1_comments.values().toArray()[1];
        CommentDetails c3 = (CommentDetails) post2_comments.values().toArray()[0];
        CommentDetails c4 = (CommentDetails) post2_comments.values().toArray()[1];

        assertEquals("com2 from user3",c1.text());
        assertEquals("com1 from user2", c2.text());
        assertEquals("com3 from user3", c3.text());
        assertEquals("com2 from user2", c4.text());
    }

    @Test
    void LinkForPostAndComments() throws Exception {
        String url = linkService.createLink(new CreateLinkCommand(1L,1L));
        PostsWithNLatestCommentsView decoded = linkService.decodeLink(url);
        Map<CommentId, CommentDetails> post_comments = decoded.getCommentsPerPost().getData().get(new PostId(1L));
        assertEquals(2,post_comments.size());
    }
}
