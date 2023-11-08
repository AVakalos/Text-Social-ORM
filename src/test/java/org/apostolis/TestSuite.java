package org.apostolis;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apostolis.comments.CommentsTest;
import org.apostolis.posts.PostsTest;
import org.apostolis.users.AccountTest;
import org.apostolis.users.FollowsTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.testcontainers.containers.PostgreSQLContainer;

@Suite
@SelectClasses({AccountTest.class, FollowsTest.class, PostsTest.class, CommentsTest.class})
public class TestSuite {

    public static AppConfig appConfig = new AppConfig("test");

    static PostgreSQLContainer<?> postgresTestDb = new PostgreSQLContainer<>("postgres:15-alpine");

    private static void hikariCPSetup(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresTestDb.getJdbcUrl());
        config.setUsername(postgresTestDb.getUsername());
        config.setPassword(postgresTestDb.getPassword());
        AppConfig.setHikariDataSource(new HikariDataSource(config));
    }

    private static void dbSchemaSetup(){
        String path = "src/main/resources/initialize schema.sql";
        try{
            ScriptRunner scriptRunner = new ScriptRunner(AppConfig.getConnection());
            scriptRunner.setSendFullScript(false);
            scriptRunner.setStopOnError(true);
            scriptRunner.runScript(new java.io.FileReader(path));
        }catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public static void initialDbSetup(){
        postgresTestDb.start();
        hikariCPSetup();
        dbSchemaSetup();
    }
}