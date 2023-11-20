package org.apostolis.posts.adapter.out.persistence;

import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.common.DbUtils;
import org.apostolis.common.HibernateUtil;
import org.apostolis.exception.DatabaseException;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.Post;
import org.apostolis.posts.domain.PostCreationException;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.domain.User;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;

public class PostRepositoryImpl implements PostRepository {

    private final DbUtils dbUtils;

    private static final Logger logger = LoggerFactory.getLogger(PostRepositoryImpl.class);

    public PostRepositoryImpl(DbUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public void savePost(Post postToSave) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        sessionFactory.inTransaction((session -> {
            UserEntity postCreator = session.getReference(UserEntity.class, postToSave.user());
            session.persist(new PostEntity(postCreator, postToSave.text(),postToSave.createdAt()));
        }));
    }

    @Override
    public HashMap<Long, HashMap<Long,String>> getPostsGivenUsersIds(ArrayList<Long> user_ids, int pageNum, int pageSize) {
//        DbUtils.ThrowingFunction<Connection, HashMap<Long,HashMap<Long,String>>, Exception> getPosts = (conn) -> {
//
//            StringBuilder query = new StringBuilder("""
//                    WITH numbered_posts AS(
//                        SELECT *, row_number() over (
//                            partition by user_id
//                            ORDER BY createdat DESC
//                        ) AS row_number
//                    FROM posts)
//                    SELECT * FROM numbered_posts
//                    WHERE user_id IN(""");
//            for(long id: user_ids){
//                query.append(id).append(",");
//            }
//            query.setCharAt(query.lastIndexOf(","),')');
//            query.append(" AND row_number > ? and row_number <= ?");
//
//
//            HashMap<Long,HashMap<Long,String>> results = new LinkedHashMap<>();
//            try(PreparedStatement stm = conn.prepareStatement(query.toString())){
//                stm.setInt(1,pageNum*pageSize);
//                stm.setInt(2, pageSize*(pageNum+1));
//
//                ResultSet rs = stm.executeQuery();
//                while(rs.next()){
//                    long user_id = rs.getInt("user_id");
//
//                    long post_id = rs.getInt("post_id");
//                    String text = rs.getString("text");
//                    if(!results.containsKey(user_id)) {
//                        results.put(user_id, new LinkedHashMap<>());
//                    }
//                    results.get(user_id).put(post_id,text);
//                }
//
//            }
//            return results;
//        };
//        try{
//            return dbUtils.doInTransaction(getPosts);
//        }catch(Exception e){
//            logger.error(e.getMessage());
//            throw new DatabaseException(e.getMessage());
//        }

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        return sessionFactory.fromTransaction(session -> {
            StringBuilder query = new StringBuilder("""
                with
                    numbered_posts as(
                        select *,row_number()\s
                            over (partition by user.user_id\s
                                  order by createdAt desc)
                            as row_number
                        from PostEntity
                    ) 
                select * from numbered_posts
                where user_id IN(:user_ids)""");

            List<Object[]> query_results = session.createSelectionQuery(query.toString(), Object[].class)
                    .setParameter("user_ids",user_ids)
                    .setFirstResult(0)
                    .setMaxResults(pageSize)
                    .getResultList();

            HashMap<Long,HashMap<Long,String>> results = new LinkedHashMap<>();

            return results;
        });


    }





    @Override
    public void registerLink(long post) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        sessionFactory.inTransaction(session -> {
            PostEntity sharedPost = session.getReference(PostEntity.class, post);
            sharedPost.setShared();
            session.merge(sharedPost);
        });

    }

    @Override
    public boolean checkLink(long post) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        return sessionFactory.fromTransaction(session -> {
            PostEntity linkPost = session.get(PostEntity.class, post);
            return linkPost.isShared;
        });

    }

    @Override
    public boolean isMyPost(long user, long post) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        return sessionFactory.fromTransaction(session -> {
            UserEntity userEntity = session.get(UserEntity.class, user);
            PostEntity sharedPost = session.getReference(PostEntity.class, post);
            return userEntity.getUser_posts().contains(sharedPost);
        });
    }
}
