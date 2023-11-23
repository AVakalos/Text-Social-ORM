package org.apostolis.posts.adapter.out.persistence;

import org.apostolis.common.DbUtils;
import org.apostolis.common.HibernateUtil;
import org.apostolis.common.PageRequest;
import org.apostolis.exception.DatabaseException;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.Post;
import org.apostolis.posts.domain.PostInfo;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PostRepositoryImpl implements PostRepository {

    private static final Logger logger = LoggerFactory.getLogger(PostRepositoryImpl.class);

    @Override
    public void savePost(Post postToSave) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        sessionFactory.inTransaction((session -> {
            UserEntity postCreator = session.getReference(UserEntity.class, postToSave.user());
            session.persist(new PostEntity(postCreator, postToSave.text(),postToSave.createdAt()));
        }));
    }

    @Override
    public PostInfo getPostById(long post) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try{
            return sessionFactory.fromTransaction(session -> {
                String postsQuery = """
                        select new org.apostolis.posts.domain.PostInfo(user.user_id, post_id, text)
                        from PostEntity
                        where post_id = :post""";

                return session.createSelectionQuery(postsQuery, PostInfo.class)
                        .setParameter("post",post)
                        .getSingleResult();
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve post by user id",e);
        }
    }


    @Override
    public HashMap<Long, HashMap<Long, String>> getPostsGivenUsersIds(List<Long> user_ids, PageRequest req) {

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try {
            return sessionFactory.fromTransaction(session -> {
                String postsQuery = new StringBuilder("""
                        select user.user_id, post_id, text
                        from PostEntity
                        where user.user_id in(:user_ids)
                        order by createdAt DESC""").toString();

                List<Object[]> query_results = session.createQuery(postsQuery, Object[].class)
                        .setParameter("user_ids", user_ids)
                        .setFirstResult(req.pageNumber() * req.pageSize())
                        .setMaxResults(req.pageSize())
                        .getResultList();

                HashMap<Long, HashMap<Long, String>> results = new LinkedHashMap<>();
//                for(PostInfo post: query_results){
//                    System.out.println(post.text());
//                }

                for (var post : query_results) {
                    System.out.println(post[0] + "," + post[1] + "," + post[2]);
                    long user_id = (Long) post[0];
                    long post_id = (Long) post[1];
                    if (!results.containsKey(user_id)) {
                        results.put(user_id, new LinkedHashMap<>());
                    }
                    results.get(user_id).put(post_id, (String) post[2]);
                }
                return results;
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve posts by user ids",e);
        }
    }



    @Override
    public void registerLink(long post) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try {
            sessionFactory.inTransaction(session -> {
                PostEntity sharedPost = session.getReference(PostEntity.class, post);
                sharedPost.setShared();
                session.merge(sharedPost);
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not save registered link",e);
        }
    }

    @Override
    public boolean checkLink(long post) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try {
            return sessionFactory.fromTransaction(session -> {
                PostEntity linkPost = session.get(PostEntity.class, post);
                return linkPost.isShared;
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not find link",e);
        }
    }

    @Override
    public boolean isMyPost(long user, long post) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try {
            return sessionFactory.fromTransaction(session -> {
                UserEntity userEntity = session.get(UserEntity.class, user);
                PostEntity sharedPost = session.getReference(PostEntity.class, post);
                return userEntity.getUser_posts().contains(sharedPost);
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could check if post belongs to user",e);
        }
    }
}
