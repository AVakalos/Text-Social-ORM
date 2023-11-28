package org.apostolis;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apostolis.comments.adapter.in.web.CreateCommentController;
import org.apostolis.comments.adapter.in.web.ViewCommentsController;
import org.apostolis.comments.adapter.out.persistence.CommentRepositoryImpl;
import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.comments.application.ports.in.CreateCommentUseCase;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.application.service.CommentService;
import org.apostolis.comments.application.service.CommentsViewService;
import org.apostolis.common.HibernateUtil;
import org.apostolis.posts.adapter.in.web.CreatePostController;
import org.apostolis.posts.adapter.in.web.ManageLinkController;
import org.apostolis.posts.adapter.in.web.ViewPostsController;
import org.apostolis.posts.adapter.out.persistence.PostRepositoryImpl;
import org.apostolis.posts.application.ports.in.CreatePostUseCase;
import org.apostolis.posts.application.ports.in.ManageLinkUseCase;
import org.apostolis.posts.application.ports.in.PostViewsUseCase;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.application.service.LinkService;
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
import org.hibernate.SessionFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Properties;

@Getter
public class AppConfig {

    private final AccountController accountController;

    private final FollowsController followsController;

    private final GetFollowsController getFollowsController;

    private final CreatePostController createPostController;

    private final CreateCommentController createCommentController;

    private final ViewPostsController viewPostsController;

    private final ViewCommentsController viewCommentsController;

    private final ManageLinkController manageLinkController;


    private final PasswordEncoder passwordEncoder;


    private final TokenManager tokenManager;

    @Getter
    private static int FREE_POST_SIZE;
    @Getter
    private static int PREMIUM_POST_SIZE;
    @Getter
    private static int FREE_MAX_COMMENTS;
    @Getter
    private static SessionFactory sessionFactory;
    @Getter
    private static HikariDataSource ds;
    @Getter
    private static final Clock clock = Clock.system(ZoneId.of("Europe/Athens"));

    private final RegisterUseCase registerService;

    private final LoginUseCase loginService;

    private final FollowsUseCase followsService;

    private final GetFollowersAndUsersToFollowUseCase getFollowsService;

    private final CreatePostUseCase postService;

    private final CreateCommentUseCase commentService;

    private final PostViewsUseCase postViewsService;

    private final CommentsViewsUseCase commentsViewService;

    private final ManageLinkUseCase linkService;


    public AppConfig(String mode){
        passwordEncoder = new PasswordEncoder();
        tokenManager = new JjwtTokenManagerImpl();

        UserRepository userRepository = new UserRepositoryImpl();
        FollowsRepository followsRepository = new FollowsRepositoryImpl();
        CommentRepository commentRepository = new CommentRepositoryImpl();
        PostRepository postRepository = new PostRepositoryImpl();

        registerService = new RegisterService(userRepository, passwordEncoder);
        loginService = new LoginService(userRepository, tokenManager, passwordEncoder);
        followsService = new FollowsService(followsRepository);
        getFollowsService = new GetFollowsService(followsRepository);
        postService = new PostService(postRepository);
        commentService = new CommentService(commentRepository);
        postViewsService = new PostViewService(postRepository, followsRepository, commentRepository);
        commentsViewService = new CommentsViewService(commentRepository, postRepository, followsRepository);
        linkService = new LinkService(postRepository, postViewsService);

        accountController = new AccountController(registerService, loginService);
        followsController = new FollowsController(followsService,tokenManager);
        getFollowsController = new GetFollowsController(getFollowsService,tokenManager);
        createPostController = new CreatePostController(postService, tokenManager);
        createCommentController = new CreateCommentController(commentService, tokenManager);
        viewPostsController = new ViewPostsController(postViewsService);
        viewCommentsController = new ViewCommentsController(commentsViewService);
        manageLinkController = new ManageLinkController(linkService);

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
            HibernateUtil.initializeSessionFactory();
            sessionFactory = HibernateUtil.getSessionFactory();

        } else if (mode.equals("test")) {
            FREE_POST_SIZE = 20;
            PREMIUM_POST_SIZE = 30;
            FREE_MAX_COMMENTS = 2;
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

    public static void setHikariDataSource(HikariDataSource ds) {
        AppConfig.ds = ds;
    }

    public static void setSessionFactory(SessionFactory sessionFactory) {
        AppConfig.sessionFactory = sessionFactory;
    }

}
