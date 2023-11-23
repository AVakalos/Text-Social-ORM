package org.apostolis.comments.adapter.out.persistence;

import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.common.DbUtils;
import org.apostolis.common.HibernateUtil;
import org.apostolis.common.PageRequest;
import org.apostolis.exception.DatabaseException;
import org.apostolis.posts.adapter.out.persistence.PostEntity;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class CommentRepositoryImpl implements CommentRepository {

    private final DbUtils dbUtils;

    private static final Logger logger = LoggerFactory.getLogger(CommentRepositoryImpl.class);

    public CommentRepositoryImpl(DbUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public void saveComment(Comment commentToSave) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        sessionFactory.inTransaction((session -> {
            UserEntity commentCreator = session.getReference(UserEntity.class, commentToSave.user());
            PostEntity post = session.getReference(PostEntity.class, commentToSave.post());
            session.persist(new CommentEntity(post, commentCreator, commentToSave.text(), commentToSave.createdAt()));
        }));
    }

    @Override
    public long getCountOfUserCommentsUnderThisPost(long user, long post) {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        return sessionFactory.fromTransaction(session ->
                session.createSelectionQuery(
                        "select count(*) from CommentEntity where post.post_id = :post",Long.class)
                .setParameter("post",post)
                .getSingleResult());
    }

    @Override
    public HashMap<Long, HashMap<Long,String>> getCommentsGivenPostIds(ArrayList<Long> post_ids, PageRequest req) {

//        DbUtils.ThrowingFunction<Connection, HashMap<Long,HashMap<Long,String>>, Exception> getComments = (conn)->{
//
//            StringBuilder query = new StringBuilder("""
//                    WITH numbered_comments AS(
//                        SELECT *, row_number() over (
//                            partition by post_id
//                            ORDER BY created DESC
//                        ) AS row_number
//                    FROM comments)
//                    SELECT * FROM numbered_comments
//                    WHERE post_id IN(""");
//            for(long id: post_ids){
//                query.append(id).append(",");
//            }
//            query.setCharAt(query.lastIndexOf(","),')');
//            query.append("AND row_number > ? and row_number <= ?");
//            HashMap<Long,HashMap<Long,String>> results = new LinkedHashMap<>();
//            try(PreparedStatement stm = conn.prepareStatement(query.toString())){
//                stm.setInt(1,pageNum*pageSize);
//                stm.setInt(2, pageSize*(pageNum+1));
//                ResultSet rs = stm.executeQuery();
//                while(rs.next()){
//                    long id = rs.getInt("post_id");
//                    long comment_id = rs.getLong("comment_id");
//                    String text = rs.getString("text");
//                    if(!results.containsKey(id)) {
//                        results.put(id, new LinkedHashMap<>());
//                    }
//                    results.get(id).put(comment_id,text);
//                }
//            }
//            return results;
//        };
//        try{
//            return dbUtils.doInTransaction(getComments);
//        }catch(Exception e){
//            logger.error(e.getMessage());
//            throw new DatabaseException(e.getMessage());
//        }
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        try{
            return sessionFactory.fromTransaction(session -> {
                String commentsQuery = new StringBuilder("""
                        select post.post_id as pid, comment_id, text
                        from CommentEntity
                        where post.post_id in(:post_ids)
                        order by createdAt DESC""").toString();
                List<Object[]> query_results = session.createQuery(commentsQuery, Object[].class)
                        .setParameter("post_ids", post_ids)
                        .setFirstResult(req.pageNumber() * req.pageSize())
                        .setMaxResults(req.pageSize())
                        .getResultList();

                HashMap<Long, HashMap<Long, String>> results = new LinkedHashMap<>();

                for (var comment : query_results) {
                    System.out.println(comment[0] + "," + comment[1] + "," + comment[2]);
                    long post_id = (Long) comment[0];
                    long comment_id = (Long) comment[1];
                    if (!results.containsKey(post_id)) {
                        results.put(post_id, new LinkedHashMap<>());
                    }
                    results.get(post_id).put(comment_id, (String) comment[2]);
                }
                return results;

            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve comments by post ids",e);
        }
    }
}
