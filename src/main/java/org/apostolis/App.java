package org.apostolis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.ExceptionHandler;
import jakarta.validation.ConstraintViolationException;
import org.apostolis.comments.adapter.in.web.CreateCommentController;
import org.apostolis.comments.adapter.in.web.ViewCommentsController;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.common.HibernateUtil;
import org.apostolis.exception.AuthenticationException;
import org.apostolis.exception.DatabaseException;
import org.apostolis.exception.InvalidTokenException;
import org.apostolis.posts.adapter.in.web.CreatePostController;
import org.apostolis.posts.adapter.in.web.ManageLinkController;
import org.apostolis.posts.adapter.in.web.ViewPostsController;
import org.apostolis.posts.domain.PostCreationException;
import org.apostolis.users.adapter.in.web.AccountController;
import org.apostolis.users.adapter.in.web.FollowsController;
import org.apostolis.users.adapter.in.web.GetFollowsController;


public class App 
{
    public static ThreadLocal<Integer> currentUserId = new ThreadLocal<>();
    public static void main( String[] args )
    {
        AppConfig appConfig = new AppConfig("production");
        HibernateUtil.initializeSessionFactory();

        AccountController accountController = appConfig.getUserController();
        FollowsController followsController = appConfig.getFollowsController();
        GetFollowsController getFollowsController = appConfig.getGetFollowsController();
        CreatePostController createPostController = appConfig.getCreatePostController();
        CreateCommentController createCommentController = appConfig.getCreateCommentController();
        ViewPostsController viewPostsController = appConfig.getPostViewsController();
        ViewCommentsController viewCommentsController = appConfig.getViewCommentsController();
        ManageLinkController manageLinkController = appConfig.getManageLinkController();

        int port = Integer.parseInt(AppConfig.readProperties().getProperty("port"));

        Javalin app = Javalin.create().start(port);

        // Global Exception Handling
        ExceptionHandler<Exception> handler = (e, ctx)->{throw new BadRequestResponse(e.getMessage());};

        app.exception(InvalidTokenException.class,handler);
        app.exception(AuthenticationException.class,handler);
        app.exception(DatabaseException.class,handler);
        app.exception(IllegalArgumentException.class,handler);
        app.exception(ConstraintViolationException.class,handler);
        app.exception(PostCreationException.class,handler);
        app.exception(CommentCreationException.class,handler);

        app.exception(ValueInstantiationException.class,
                (e, ctx)->{throw new BadRequestResponse("Invalid JSON data:\n" + e.getCause().getMessage());});
        app.exception(JsonParseException.class,
                (e, ctx)->{throw new BadRequestResponse("Use a valid JSON format in the request body");});
        app.exception(JsonMappingException.class,
                (e, ctx)->{throw new BadRequestResponse("Text contains illegal characters");});

        app.post("/signup", accountController::signup);
        app.post("/signin", accountController::login);

        app.before("/api/*", accountController::authenticate);

        app.post("/api/newpost",createPostController::createPost);
        app.post("/api/newcomment",createCommentController::createComment);
        app.post("/api/follow", followsController::follow);
        app.delete("/api/unfollow", followsController::unfollow);
        app.get("/api/user/createurl/{post}",manageLinkController::createLink);

        app.get("api/user/following/posts", viewPostsController::getFollowingPosts);
        app.get("api/user/posts", viewPostsController::getOwnPostsWithLatestNComments);
        app.get("api/user/posts/comments",viewCommentsController::getCommentsOnOwnPosts);
        app.get("api/user/posts/latestcomments",viewCommentsController::getLatestCommentsOnOwnOrFollowingPosts);
        app.get("api/user/followers",getFollowsController::getFollowers);
        app.get("api/user/tofollow",getFollowsController::getUsersToFollow);

        app.get("<url>",manageLinkController::decodeLink);


        //PostRepositoryImpl postRepository = new PostRepositoryImpl(appConfig.getDbUtils(), followsRepository);
       // CommentRepository commentRepository = new CommentRepositoryImpl(appConfig.getDbUtils());

        //ArrayList<Integer> ids = new ArrayList<>(List.of(1,2,3));
        //System.out.println(postRepository.getPostsGivenUsersIds(ids,0,4));
        //System.out.println(commentRepository.getLatestCommentsGivenPostIds(ids));
    }
}
