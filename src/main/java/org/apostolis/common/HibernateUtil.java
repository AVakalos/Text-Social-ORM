package org.apostolis.common;

import net.ttddyy.dsproxy.listener.logging.DefaultQueryLogEntryCreator;
import net.ttddyy.dsproxy.listener.logging.SLF4JLogLevel;
import net.ttddyy.dsproxy.listener.logging.SLF4JQueryLoggingListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.apostolis.AppConfig;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.hibernate.engine.jdbc.internal.Formatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HibernateUtil {
    private static StandardServiceRegistry registry;
    private static SessionFactory sessionFactory;

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);

    public static SessionFactory getSessionFactory() {

        if (sessionFactory == null) {

            StandardServiceRegistryBuilder standardRegistryBuilder = new StandardServiceRegistryBuilder();
            DataSource  datasource = getDataSource();

            Map<String, Object> settings = new HashMap<>();
            settings.put(Environment.JAKARTA_JTA_DATASOURCE, datasource);

            registry = standardRegistryBuilder.applySettings(settings).build();

            try {
                sessionFactory =
                        new MetadataSources(registry)
                                .addAnnotatedClass(UserEntity.class)
                                .buildMetadata()
                                .buildSessionFactory();


            } catch (Exception e) {
                StandardServiceRegistryBuilder.destroy(registry);
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (registry != null) {
            StandardServiceRegistryBuilder.destroy(registry);
        }
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
                .logQueryToSysOut()
                .asJson()
                .countQuery()
                .multiline()
                .listener(listener)
                .build();
    }

//    private static DataSource getPostgresDataSource() {
//        PGSimpleDataSource ds = new PGSimpleDataSource();
//        ds.setURL("jdbc:postgresql://localhost:5433/TextSocial");
//        ds.setUser("postgres");
//        ds.setPassword("1234");
//        return ds;
//    }

    private static class PrettyQueryEntryCreator extends DefaultQueryLogEntryCreator {
        private final Formatter formatter = FormatStyle.BASIC.getFormatter();

        @Override
        protected String formatQuery(String query) {
            return this.formatter.format(query);
        }
    }
}
