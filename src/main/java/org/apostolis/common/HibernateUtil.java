package org.apostolis.common;

import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.apostolis.AppConfig;
import org.apostolis.comments.adapter.out.persistence.CommentEntity;
import org.apostolis.posts.adapter.out.persistence.PostEntity;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.concurrent.TimeUnit;

// Hibernate ORM and Datasource proxy (queries monitoring) configuration
public class HibernateUtil {
    private static SessionFactory sessionFactory;

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

    public static void initializeSessionFactory(){
        if (sessionFactory == null) {
            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .applySetting(Environment.DATASOURCE, getDataSource())
                    .build();

            Metadata metadata = new MetadataSources(standardRegistry)
                    .addAnnotatedClass(UserEntity.class)
                    .addAnnotatedClass(PostEntity.class)
                    .addAnnotatedClass(CommentEntity.class)
                    .getMetadataBuilder()
                    .build();

            SessionFactoryBuilder sessionFactoryBuilder = metadata.getSessionFactoryBuilder();

            sessionFactory = sessionFactoryBuilder.build();
        }
    }

    public static SessionFactory getSessionFactory(){
        if (sessionFactory == null) {
            logger.error("Session Factory is null");
            initializeSessionFactory();
        }
        return sessionFactory;
    }

    private static DataSource getDataSource() {
        // use pretty formatted query with multiline enabled
        PrettyQueryEntryCreator creator = new PrettyQueryEntryCreator();
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
}
