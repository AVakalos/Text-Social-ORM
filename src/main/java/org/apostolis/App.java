package org.apostolis;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import jakarta.validation.ConstraintViolationException;
import org.apostolis.exception.DatabaseException;
import org.apostolis.exception.InvalidTokenException;
import org.apostolis.users.adapter.in.web.AccountController;
import org.apostolis.users.adapter.in.web.FollowsController;
import org.apostolis.users.adapter.in.web.GetFollowsController;


public class App 
{
    public static ThreadLocal<Integer> currentUserId = new ThreadLocal<>();
    public static void main( String[] args )
    {
        AppConfig appConfig = new AppConfig("production");
        AccountController accountController = appConfig.getUserController();
        FollowsController followsController = appConfig.getFollowsController();
        GetFollowsController getFollowsController = appConfig.getGetFollowsController();
        int port = Integer.parseInt(AppConfig.readProperties().getProperty("port"));

        Javalin app = Javalin.create().start(port);


        // Global Exception Handling
        app.exception(InvalidTokenException.class,(e, ctx)->{throw new BadRequestResponse(e.getMessage());});
        app.exception(DatabaseException.class,(e, ctx)->{throw new BadRequestResponse(e.getMessage());});
        app.exception(IllegalArgumentException.class,(e, ctx)->{throw new BadRequestResponse(e.getMessage());});
        app.exception(ConstraintViolationException.class,(e, ctx)->{throw new BadRequestResponse(e.getMessage());});
        app.exception(ValueInstantiationException.class,(e, ctx)->{throw new BadRequestResponse(e.getCause().getMessage());});
        app.exception(JsonParseException.class,(e, ctx)->{throw new BadRequestResponse("Use a valid JSON format in the request body");});

        app.post("/signup", accountController::signup);
        app.post("/signin", accountController::login);

        app.before("/api/*", accountController::authenticate);

//        app.post("/api/newpost",operationsController::createPost);
//        app.post("/api/newcomment",operationsController::createComment);
        app.post("/api/follow", followsController::follow);
        app.delete("/api/unfollow", followsController::unfollow);
//        app.get("/api/user/{id}/createurl/{post}",operationsController::createUrlForPostAndComments);
//
//        app.get("api/user/{id}/followers/posts", viewsController::getFollowersPostsInReverseChrono);
//        app.get("api/user/{id}/posts",viewsController::getOwnPostsWithLast100CommentsInReverseChrono);
//        app.get("api/user/{id}/posts/comments",viewsController::getAllCommentsOnOwnPosts);
//        app.get("api/user/{id}/latestcomments",viewsController::getLatestCommentsOnOwnOrFollowersPosts);
        app.get("api/user/followers",getFollowsController::getFollowers);
        app.get("api/user/tofollow",getFollowsController::getUsersToFollow);
//
//        app.get("<url>",operationsController::decodeLink);
    }
}
