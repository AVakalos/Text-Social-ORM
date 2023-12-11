package org.apostolis;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apostolis.comments.CommentsTest;
import org.apostolis.comments.adapter.out.persistence.CommentEntity;
import org.apostolis.common.TransactionUtils;
import org.apostolis.posts.PostsTest;
import org.apostolis.posts.adapter.out.persistence.PostEntity;
import org.apostolis.users.AccountTest;
import org.apostolis.users.FollowsTest;
import org.apostolis.users.adapter.out.persistence.FollowerEntity;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.SessionFactoryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

// Configuration for the testing environment.
@Suite
@SelectClasses({AccountTest.class, FollowsTest.class, PostsTest.class, CommentsTest.class})
public class TestSuite {

    public static AppConfig appConfig = new AppConfig("test");

    static PostgreSQLContainer<?> postgresTestDb = new PostgreSQLContainer<>("postgres:15-alpine");

    public static SessionFactory sessionFactory;


    private static void hikariCPSetup(){
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgresTestDb.getJdbcUrl());
        config.setUsername(postgresTestDb.getUsername());
        config.setPassword(postgresTestDb.getPassword());
        AppConfig.setHikariDataSource(new HikariDataSource(config));
    }

    // Load schema in test database
    private static void dbSchemaSetup() {
        String path = "src/main/resources/schema.sql";
        try {
            ScriptRunner scriptRunner = new ScriptRunner(AppConfig.getDs().getConnection());
            scriptRunner.setSendFullScript(false);
            scriptRunner.setStopOnError(true);
            scriptRunner.runScript(new java.io.FileReader(path));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static void hibernateConfig(){
        if (sessionFactory == null) {
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .applySetting(Environment.DATASOURCE, getDataSource()).build();

            Metadata metadata = new MetadataSources(standardRegistry)
                    .addAnnotatedClass(UserEntity.class)
                    .addAnnotatedClass(PostEntity.class)
                    .addAnnotatedClass(CommentEntity.class)
                    .addAnnotatedClass(FollowerEntity.class)
                    .getMetadataBuilder()
                    .build();

            SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();
            sessionFactory = sessionFactoryBuilder.build();
            AppConfig.setSessionFactory(sessionFactory);
        }
    }

    // Setting the datasource proxy for queries monitoring
    private static DataSource getDataSource() {
        // use pretty formatted query with multiline enabled
        TestSuite.PrettyQueryEntryCreator creator = new TestSuite.PrettyQueryEntryCreator();
        creator.setMultiline(true);
        SLF4JQueryLoggingListener listener = new SLF4JQueryLoggingListener();
        listener.setQueryLogEntryCreator(creator);

        return ProxyDataSourceBuilder.create(AppConfig.getDs())
                .logQueryBySlf4j(SLF4JLogLevel.INFO)
                .logSlowQueryBySlf4j(1, TimeUnit.MINUTES)
                .countQuery()
                .multiline()
                .listener(listener)
                .build();
    }

    private static class PrettyQueryEntryCreator extends DefaultQueryLogEntryCreator {
        private final Formatter formatter = FormatStyle.BASIC.getFormatter();

        @Override
        protected String formatQuery(String query) {
            return this.formatter.format(query);
        }
    }

    public static SessionFactory getSessionFactory(){
        if(sessionFactory!=null){
            return sessionFactory;
        }else{
            throw new NullPointerException("Session Factory is null");
        }
    }

    public static void initialDbSetup(){
        postgresTestDb.start();
        hikariCPSetup();
        dbSchemaSetup();
        hibernateConfig();
        appConfig.getTransactionUtils().setSessionFactory(sessionFactory);
    }
}