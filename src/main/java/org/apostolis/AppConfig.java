package org.apostolis;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apostolis.comments.adapter.in.web.CreateCommentController;
import org.apostolis.comments.adapter.in.web.ViewCommentsController;
import org.apostolis.comments.adapter.out.persistence.CommentRepositoryImpl;
import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.comments.application.ports.in.CreateCommentUseCase;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.application.service.CommentService;
import org.apostolis.comments.application.service.CommentsViewService;
import org.apostolis.common.DbUtils;
import org.apostolis.posts.adapter.in.web.CreatePostController;
import org.apostolis.posts.adapter.in.web.ViewPostsController;
import org.apostolis.posts.adapter.out.persistence.PostRepositoryImpl;
import org.apostolis.posts.application.ports.in.CreatePostUseCase;
import org.apostolis.posts.application.ports.in.PostViewsUseCase;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.application.service.PostService;
import org.apostolis.posts.application.service.PostViewService;
import org.apostolis.security.JjwtTokenManagerImpl;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.security.TokenManager;
import org.apostolis.users.adapter.in.web.AccountController;
import org.apostolis.users.adapter.in.web.FollowsController;
import org.apostolis.users.adapter.in.web.GetFollowsController;
import org.apostolis.users.adapter.out.persistence.FollowsRepositoryImpl;
import org.apostolis.users.adapter.out.persistence.UserRepositoryImpl;
import org.apostolis.users.application.ports.in.FollowsUseCase;
import org.apostolis.users.application.ports.in.GetFollowersAndUsersToFollowUseCase;
import org.apostolis.users.application.ports.in.LoginUseCase;
import org.apostolis.users.application.ports.in.RegisterUseCase;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.application.service.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Properties;

public class AppConfig {
    private final AccountController accountController;
    private final FollowsController followsController;
    private final GetFollowsController getFollowsController;
    private final CreatePostController createPostController;
    private final CreateCommentController createCommentController;
    private final ViewPostsController viewPostsController;
    private final ViewCommentsController viewCommentsController;

    private final DbUtils dbUtils;


    private static int FREE_POST_SIZE;
    private static int PREMIUM_POST_SIZE;
    private static int FREE_MAX_COMMENTS;



    private static HikariDataSource ds;

    public static Clock clock = Clock.system(ZoneId.of("Europe/Athens"));


    public AppConfig(String mode){
        dbUtils = new DbUtils();
        PasswordEncoder passwordEncoder = new PasswordEncoder();
        TokenManager tokenManager = new JjwtTokenManagerImpl();

//        UserPersistenceInjector userPersistence = new UserPersistenceInjector(dbUtils);
//        UserServicesInjector userServices = new UserServicesInjector(userPersistence.getUserRepository(),
//                                                                     tokenManager,
//                                                                     passwordEncoder,
//                                                                     userPersistence.getFollowsRepository());
//
//        userControllers = new UserControllersInjector(userServices.getFollowsService(),
//                                                                              userServices.getRegisterService(),
//                                                                              userServices.getLoginService(),
//                                                                              userServices.getGetFollowsService(),
//                                                                              tokenManager);

        UserRepository userRepository = new UserRepositoryImpl(dbUtils);
        FollowsRepository followsRepository = new FollowsRepositoryImpl(dbUtils);
        CommentRepository commentRepository = new CommentRepositoryImpl(dbUtils);
        PostRepository postRepository = new PostRepositoryImpl(dbUtils);


        RegisterUseCase registerService = new RegisterService(userRepository, passwordEncoder);
        LoginUseCase loginService = new LoginService(userRepository, tokenManager, passwordEncoder);
        FollowsUseCase followsService = new FollowsService(followsRepository);
        GetFollowersAndUsersToFollowUseCase getFollowsService = new GetFollowsService(followsRepository);
        CreatePostUseCase postService = new PostService(postRepository);
        CreateCommentUseCase commentService = new CommentService(commentRepository);
        PostViewsUseCase postViewsService = new PostViewService(postRepository, followsRepository, commentRepository);
        CommentsViewsUseCase commentsViewService = new CommentsViewService(postRepository, followsRepository);

//        accountController = userControllers.getAccountController();
//        followsController = userControllers.getFollowsController();
//        getFollowsController = userControllers.getGetFollowsController();

        accountController = new AccountController(registerService,loginService);
        followsController = new FollowsController(followsService,tokenManager);
        getFollowsController = new GetFollowsController(getFollowsService,tokenManager);
        createPostController = new CreatePostController(postService, tokenManager);
        createCommentController = new CreateCommentController(commentService, tokenManager);
        viewPostsController = new ViewPostsController(postViewsService);
        viewCommentsController = new ViewCommentsController(commentsViewService);

        if(mode.equals("production")){
            Properties appProps = readProperties();

            FREE_POST_SIZE = 1000;
            PREMIUM_POST_SIZE = 3000;
            FREE_MAX_COMMENTS = 5;


            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(appProps.getProperty("databaseUrl"));
            config.setUsername(appProps.getProperty("databaseUsername"));
            config.setPassword(appProps.getProperty("databasePassword"));
            ds = new HikariDataSource(config);

        } else if (mode.equals("test")) {
            Properties appProps = readProperties();

            FREE_POST_SIZE = 20;
            PREMIUM_POST_SIZE = 30;
            FREE_MAX_COMMENTS = 2;

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(appProps.getProperty("testDatabaseUrl"));
            config.setUsername(appProps.getProperty("testDatabaseUsername"));
            config.setPassword(appProps.getProperty("testDatabasePassword"));
            ds = new HikariDataSource(config);

        }else{
            throw new RuntimeException("Specify mode 'production' or 'test' when initializing AppConfig");
        }

    }

    public static Properties readProperties(){
        Properties appProps = new Properties();
        try {
            String propertiesPath = "target/classes/application.properties";
            appProps.load(Files.newInputStream(Paths.get(propertiesPath)));
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return appProps;
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static int getFreePostSize() {
        return FREE_POST_SIZE;
    }

    public static int getPremiumPostSize() {
        return PREMIUM_POST_SIZE;
    }

    public static int getFreeMaxComments() {
        return FREE_MAX_COMMENTS;
    }

    public AccountController getUserController() {
        return accountController;
    }

    public FollowsController getFollowsController() {
        return followsController;
    }

    public GetFollowsController getGetFollowsController() {
        return getFollowsController;
    }

    public CreatePostController getCreatePostController() {
        return createPostController;
    }

    public CreateCommentController getCreateCommentController() {
        return createCommentController;
    }

    public ViewPostsController getPostViewsController() {
        return viewPostsController;
    }

    public ViewCommentsController getViewCommentsController() {
        return viewCommentsController;
    }

    public DbUtils getDbUtils() {
        return dbUtils;
    }
}
