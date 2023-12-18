package org.apostolis;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import org.apostolis.comments.adapter.in.web.CreateCommentController;
import org.apostolis.comments.adapter.in.web.CommentsViewController;
import org.apostolis.comments.adapter.out.persistence.CommentViewsRepositoryImpl;
import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.comments.application.ports.in.CreateCommentUseCase;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.application.service.CommentService;
import org.apostolis.comments.application.service.CommentsViewService;
import org.apostolis.common.HibernateUtil;
import org.apostolis.common.TransactionUtils;
import org.apostolis.posts.adapter.in.web.CreatePostController;
import org.apostolis.posts.adapter.in.web.ManageLinkController;
import org.apostolis.posts.adapter.in.web.PostsViewController;
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
import org.apostolis.users.adapter.in.web.FollowsViewController;
import org.apostolis.users.adapter.out.persistence.FollowViewsRepositoryImpl;
import org.apostolis.users.adapter.out.persistence.UserRepositoryImpl;
import org.apostolis.users.application.ports.in.*;
import org.apostolis.users.application.ports.out.FollowViewsRepository;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.application.service.*;
import org.hibernate.SessionFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Properties;

// Configure application parameters and initialize objects for production and testing environments.
@Getter
public class AppConfig {

    private final AccountController accountController;

    private final FollowsController followsController;

    private final FollowsViewController followsViewController;

    private final CreatePostController createPostController;

    private final CreateCommentController createCommentController;

    private final PostsViewController postsViewController;

    private final CommentsViewController commentsViewController;

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

    private final FollowsUseCase followsService;

    private final GetFollowersAndUsersToFollowUseCase getFollowsService;

    private final CreatePostUseCase postService;

    private final CreateCommentUseCase commentService;

    private final PostViewsUseCase postViewsService;

    private final CommentsViewsUseCase commentsViewService;

    private final ManageLinkUseCase linkService;

    private final AccountManagementUseCase accountService;

    private final TransactionUtils transactionUtils;


    public AppConfig(String mode){

        // Parameters for production or testing environments.
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

        passwordEncoder = new PasswordEncoder();
        tokenManager = new JjwtTokenManagerImpl();
        transactionUtils = new TransactionUtils();


        UserRepository userRepository = new UserRepositoryImpl(transactionUtils);
        FollowViewsRepository followViewsRepository = new FollowViewsRepositoryImpl(transactionUtils);
        CommentRepository commentRepository = new CommentViewsRepositoryImpl(transactionUtils);
        PostRepository postRepository = new PostRepositoryImpl(transactionUtils);

        accountService = new AccountService(userRepository,tokenManager,passwordEncoder,transactionUtils);
        followsService = new FollowsService(userRepository,transactionUtils);
        getFollowsService = new FollowsViewService(followViewsRepository,transactionUtils);
        postService = new PostService(postRepository,transactionUtils);
        commentService = new CommentService(postRepository,transactionUtils);
        postViewsService = new PostViewService(postRepository, followViewsRepository,commentRepository,transactionUtils);
        commentsViewService = new CommentsViewService(commentRepository,postRepository, followViewsRepository,transactionUtils);
        linkService = new LinkService(postRepository,postViewsService,transactionUtils);

        accountController = new AccountController(accountService);
        followsController = new FollowsController(followsService,tokenManager);
        followsViewController = new FollowsViewController(getFollowsService,tokenManager);
        createPostController = new CreatePostController(postService,tokenManager);
        createCommentController = new CreateCommentController(commentService,tokenManager);
        postsViewController = new PostsViewController(postViewsService);
        commentsViewController = new CommentsViewController(commentsViewService);
        manageLinkController = new ManageLinkController(linkService);
    }

    // Read application.properties file utility method
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

    // Used only for testing configuration
    public static void setHikariDataSource(HikariDataSource ds) {
        AppConfig.ds = ds;
    }

    public static void setSessionFactory(SessionFactory sessionFactory) {
        AppConfig.sessionFactory = sessionFactory;
    }

}
