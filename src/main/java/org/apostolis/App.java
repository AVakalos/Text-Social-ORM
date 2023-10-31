package org.apostolis;

import io.javalin.Javalin;
import org.apostolis.users.adapter.in.web.AccountController;
import org.apostolis.users.adapter.in.web.FollowingController;


public class App 
{
    public static ThreadLocal<Integer> currentUserId = new ThreadLocal<>();
    public static void main( String[] args )
    {



        AppConfig appConfig = new AppConfig("production");
        AccountController accountController = appConfig.getUserController();
        FollowingController followingController = appConfig.getFollowingController();
        int port = Integer.parseInt(AppConfig.readProperties().getProperty("port"));

        Javalin app = Javalin.create().start(port);


        app.post("/signup", accountController::signup);
        app.post("/signin", accountController::login);

        app.before("/api/*", accountController::authenticate);

//        app.post("/api/newpost",operationsController::createPost);
//        app.post("/api/newcomment",operationsController::createComment);
        app.post("/api/follow",followingController::follow);
        app.delete("/api/unfollow",followingController::unfollow);
//        app.get("/api/user/{id}/createurl/{post}",operationsController::createUrlForPostAndComments);
//
//        app.get("api/user/{id}/followers/posts", viewsController::getFollowersPostsInReverseChrono);
//        app.get("api/user/{id}/posts",viewsController::getOwnPostsWithLast100CommentsInReverseChrono);
//        app.get("api/user/{id}/posts/comments",viewsController::getAllCommentsOnOwnPosts);
//        app.get("api/user/{id}/latestcomments",viewsController::getLatestCommentsOnOwnOrFollowersPosts);
//        app.get("api/user/{id}/followers",viewsController::getFollowersOf);
//        app.get("api/user/{id}/tofollow",viewsController::getUsersToFollow);
//
//        app.get("<url>",operationsController::decodeLink);
    }
}
