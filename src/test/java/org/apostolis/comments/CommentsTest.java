package org.apostolis.comments;

import org.apostolis.AppConfig;
import org.apostolis.TestSuite;
import org.apostolis.comments.domain.CommentId;
import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.comments.application.ports.in.CreateCommentCommand;
import org.apostolis.comments.application.ports.in.CreateCommentUseCase;
import org.apostolis.comments.application.ports.in.ViewCommentsQuery;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.comments.domain.CommentDTO;
import org.apostolis.common.PageRequest;
import org.apostolis.common.TransactionUtils;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.UserId;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.MutationQuery;
import org.junit.jupiter.api.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CommentsTest {
    static TransactionUtils transactionUtils;
    static CreateCommentUseCase commentService;
    static CommentsViewsUseCase commentsViewsService;
    private static final AppConfig appConfig = TestSuite.appConfig;

    @BeforeAll
    static void startDb(){
        TestSuite.initialDbSetup();
        transactionUtils = appConfig.getTransactionUtils();
        commentService = appConfig.getCommentService();
        commentsViewsService = appConfig.getCommentsViewService();

        TransactionUtils.ThrowingConsumer<Session,Exception> task = (session) -> {
            String truncate_tables = "TRUNCATE TABLE users, posts, followers RESTART IDENTITY CASCADE";
            session.createNativeMutationQuery(truncate_tables).executeUpdate();

            String insert_users = """
                        INSERT INTO users (username,password,role) VALUES
                        ('user1','pass','FREE'),
                        ('user2','pass','PREMIUM'),
                        ('user3','pass','FREE')""";
            session.createNativeMutationQuery(insert_users).executeUpdate();

            String insert_posts = """
                        INSERT INTO posts (user_id, text, isshared, createdat) VALUES
                        (1,'post1 from user1',false,?1),
                        (1,'post2 from user1',false,?2),
                        (2,'post1 from user2',false,?3),
                        (3,'post1 from user3',false,?4)""";

            MutationQuery posts_query = session.createNativeMutationQuery(insert_posts);
            for (int i = 1; i <= 4; i++) {
                posts_query.setParameter(i, Timestamp.valueOf(
                        LocalDateTime.now(AppConfig.getClock()).plusSeconds(i * 30)));
            }
            posts_query.executeUpdate();

            // Populate followers table
            String insert_follows = "INSERT INTO followers VALUES(2,1),(3,1),(1,2),(2,3)";
            session.createNativeMutationQuery(insert_follows).executeUpdate();
        };
        try {
            transactionUtils.doInTransaction(task);
        }catch(Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    @BeforeEach
    void intermediateSetupDatabase(){
        TransactionUtils.ThrowingConsumer<Session,Exception> task = (session) -> {
            String truncate_tables = "TRUNCATE TABLE comments RESTART IDENTITY CASCADE";
            session.createNativeMutationQuery(truncate_tables).executeUpdate();

            String insert_comments = """
                        INSERT INTO comments (post_id,user_id,text,createdat) VALUES
                        (1,2,'com1 from user2',?1),
                        (2,1,'com1 from user1',?2),
                        (3,1,'com1 from user1',?3),
                        (1,3,'com2 from user3',?4),
                        (2,2,'com2 from user2',?5),
                        (2,3,'com3 from user3',?6)""";

            MutationQuery comments_query = session.createNativeMutationQuery(insert_comments);
            for(int i=1; i<=6; i++){
                comments_query.setParameter(i, Timestamp.valueOf(
                        LocalDateTime.now(AppConfig.getClock()).plusSeconds(120 + i*30)), Timestamp.class);
            }
            comments_query.executeUpdate();
        };
        try {
            transactionUtils.doInTransaction(task);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void commentFromFreeUser(){
        CreateCommentCommand createCommentCommand = new CreateCommentCommand(new UserId(1L),new PostId(1L),"first comment","FREE");
        assertDoesNotThrow(()->commentService.createComment(createCommentCommand));
    }

    @Test
    void commentFromFreeUserExceedLimit(){
        CreateCommentCommand createCommentCommand1 = new CreateCommentCommand(new UserId(1L),new PostId(1L),"comment1","FREE");
        CreateCommentCommand createCommentCommand2 = new CreateCommentCommand(new UserId(1L),new PostId(1L),"comment2","FREE");
        CreateCommentCommand createCommentCommand3 = new CreateCommentCommand(new UserId(1L),new PostId(1L),"comment3","FREE");

        assertThrows(CommentCreationException.class, () -> {
            commentService.createComment(createCommentCommand1);
            commentService.createComment(createCommentCommand2);
            commentService.createComment(createCommentCommand3);
        });
    }

    @Test
    void commentFromPremiumUser(){
        CreateCommentCommand createCommentCommand1 = new CreateCommentCommand(new UserId(2L),new PostId(1L),"comment1","PREMIUM");
        CreateCommentCommand createCommentCommand2 = new CreateCommentCommand(new UserId(2L),new PostId(1L),"comment2","PREMIUM");
        CreateCommentCommand createCommentCommand3 = new CreateCommentCommand(new UserId(2L),new PostId(1L),"comment3","PREMIUM");

        assertDoesNotThrow(() -> {
            commentService.createComment(createCommentCommand1);
            commentService.createComment(createCommentCommand2);
            commentService.createComment(createCommentCommand3);
        });
    }

    @Test
    void getAllCommentsOnOwnPosts() throws Exception {
        ViewCommentsQuery viewCommentsQuery = new ViewCommentsQuery(
                new UserId(1L),new PageRequest(0,Integer.MAX_VALUE));
        Map<PostId, List<Object>> result = commentsViewsService.getCommentsOnOwnPosts(viewCommentsQuery);

        Map<CommentId, CommentDTO> post1_comments = (HashMap)result.get(new PostId(2L)).get(1);
        Map<CommentId, CommentDTO> post2_comments = (HashMap)result.get(new PostId(1L)).get(1);

        assertEquals(2,result.size());
        assertEquals(5,post1_comments.size()+post2_comments.size());
    }

    @Test
    void getLatestCommentsOnOwnOrFollowersPosts() throws Exception {
        ViewCommentsQuery viewCommentsQuery = new ViewCommentsQuery(
                new UserId(2L),new PageRequest(0,Integer.MAX_VALUE));

        Map<UserId, Map<PostId,List<Object>>> result =
                commentsViewsService.getLatestCommentsOnOwnOrFollowingPosts(viewCommentsQuery);

        int total_size_of_posts = result.get(new UserId(1L)).size() +
                                  result.get(new UserId(3L)).size() +
                                  result.get(new UserId(2L)).size();

        assertEquals(4,total_size_of_posts);
    }
}
