package org.apostolis.common;

import lombok.Getter;
import lombok.Setter;
import org.apostolis.AppConfig;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Objects.isNull;


public class TransactionUtils {
    @Setter
    private SessionFactory sessionFactory;
    private static final ThreadLocal<Session> thlsession = new ThreadLocal<>();
    private static final Logger logger = LoggerFactory.getLogger(TransactionUtils.class);

    // Custom Functional Interfaces to handle Exceptions in lambda expressions
    @FunctionalInterface
    public interface ThrowingConsumer<T, E extends Exception> {
        void accept(T t) throws E;
    }

    // Extend functional interfaces to throw Exceptions
    @FunctionalInterface
    public interface ThrowingFunction<T, R, E extends Exception>{
        R apply(T t) throws E;
    }

    public TransactionUtils(){
        sessionFactory = AppConfig.getSessionFactory();
    }

    // Overloaded function for transaction management
    // Function lambda
    public <R> R doInTransaction(ThrowingFunction<Session, R, Exception> dbtask) throws Exception {

        boolean is_parent_transaction = false;
        if (isNull(thlsession.get())){
            Session session = sessionFactory.openSession();
            thlsession.set(session);
            is_parent_transaction = true;
        }
        Session session = thlsession.get();
        Transaction tx = null;
        R rs;

        try{
            if(is_parent_transaction){
                tx = session.beginTransaction();
            }else{
                tx = session.getTransaction();
            }
            rs = dbtask.apply(session);
            if (is_parent_transaction) {
                tx.commit();
            }

        }catch (Exception e){
            logger.error(e.getMessage());
            logger.error("Rolling back the transaction.");
            if(tx!=null){
                tx.rollback();
            }
            thlsession.remove();
            throw e;
        }
        if(is_parent_transaction){
            thlsession.remove();
        }
        return rs;
    }

    // Consumer lambda
    public void doInTransaction(ThrowingConsumer<Session, Exception> dbtask) throws Exception {

        boolean is_parent_transaction = false;
        if (isNull(thlsession.get())){
            Session session = sessionFactory.openSession();
            thlsession.set(session);
            is_parent_transaction = true;
        }
        Session session = thlsession.get();
        Transaction tx = null;

        try{
            if(is_parent_transaction){
                tx = session.beginTransaction();
            }else{
                tx = session.getTransaction();
            }
            dbtask.accept(session);
            if (is_parent_transaction) {
                tx.commit();
            }

        }catch(Exception e){
            logger.error(e.getMessage());
            logger.error("Rolling back the transaction.");
            if(tx!=null){
                tx.rollback();
            }
            thlsession.remove();
            throw e;
        }
        if(is_parent_transaction){
            thlsession.remove();
        }
    }
}
