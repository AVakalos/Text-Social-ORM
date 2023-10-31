package org.apostolis;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apostolis.common.DbUtils;
import org.apostolis.security.JjwtTokenManagerImpl;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.security.TokenManager;
import org.apostolis.users.adapter.in.web.AccountController;
import org.apostolis.users.adapter.in.web.FollowingController;
import org.apostolis.users.adapter.out.persistence.FollowingRepositoryImpl;
import org.apostolis.users.adapter.out.persistence.UserRepositoryImpl;
import org.apostolis.users.application.ports.in.FollowUseCase;
import org.apostolis.users.application.ports.in.LoginUseCase;
import org.apostolis.users.application.ports.in.RegisterUseCase;
import org.apostolis.users.application.ports.out.FollowingRepository;
import org.apostolis.users.application.ports.out.UserRepository;
import org.apostolis.users.application.service.FollowingService;
import org.apostolis.users.application.service.LoginService;
import org.apostolis.users.application.service.RegisterService;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Properties;

public class AppConfig {
    private final DbUtils dbUtils;

    private final UserRepository userRepository;

    private final FollowingRepository followingRepository;

    private final TokenManager tokenManager;

    private final PasswordEncoder passwordEncoder;

    private final RegisterUseCase registerService;

    private final LoginUseCase loginService;

    private final FollowUseCase followingService;

    private final AccountController accountController;

    private final FollowingController followingController;




    private static HikariDataSource ds;

    public static Clock clock = Clock.system(ZoneId.of("Europe/Athens"));


    public AppConfig(String mode){
        dbUtils = new DbUtils();
        passwordEncoder = new PasswordEncoder();
        tokenManager = new JjwtTokenManagerImpl();
        userRepository = new UserRepositoryImpl(dbUtils);
        followingRepository = new FollowingRepositoryImpl(dbUtils);
        registerService = new RegisterService(userRepository, passwordEncoder);
        loginService = new LoginService(userRepository, tokenManager, passwordEncoder);
        accountController = new AccountController(registerService, loginService);
        followingService = new FollowingService(followingRepository, userRepository);
        followingController = new FollowingController(followingService, tokenManager);





        if(mode.equals("production")){
            Properties appProps = readProperties();

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(appProps.getProperty("databaseUrl"));
            config.setUsername(appProps.getProperty("databaseUsername"));
            config.setPassword(appProps.getProperty("databasePassword"));
            ds = new HikariDataSource(config);

        } else if (mode.equals("test")) {
            Properties appProps = readProperties();

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


    public DbUtils getDbUtils() {
        return dbUtils;
    }

    public AccountController getUserController() {
        return accountController;
    }

    public FollowingController getFollowingController() {
        return followingController;
    }
}
