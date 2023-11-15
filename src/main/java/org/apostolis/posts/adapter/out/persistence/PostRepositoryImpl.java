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
//        DbUtils.ThrowingConsumer<Connection,Exception> savePostIntoDb = (conn) -> {
//            try(PreparedStatement savepost_stm = conn.prepareStatement(
//                    "INSERT INTO posts (user_id, text, created) VALUES (?,?,?)")){
//                savepost_stm.setInt(1,postToSave.user());
//                savepost_stm.setString(2, postToSave.text());
//                savepost_stm.setTimestamp(3, Timestamp.valueOf(postToSave.createdAt()));
//                savepost_stm.executeUpdate();
//            }
//        };
//        try{
//            dbUtils.doInTransaction(savePostIntoDb);
//            logger.info("Post saved successfully in the database.");
//        }catch (Exception e){
//            logger.error("Post didn't saved.");
//            throw new PostCreationException(e.getMessage(),e);
//        }
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        sessionFactory.inTransaction((session -> {
            UserEntity postCreator = session.getReference(UserEntity.class, postToSave.user());
            session.persist(new PostEntity(postCreator, postToSave.text(),postToSave.createdAt()));
        }));
    }

    @Override
    public HashMap<Integer, HashMap<Integer,String>> getPostsGivenUsersIds(ArrayList<Integer> user_ids, int pageNum, int pageSize) {
        DbUtils.ThrowingFunction<Connection, HashMap<Integer,HashMap<Integer,String>>, Exception> getPosts = (conn) -> {

            StringBuilder query = new StringBuilder("""
                    WITH numbered_posts AS(
                        SELECT *, row_number() over (
                            partition by user_id
                            ORDER BY createdat DESC
                        ) AS row_number
                    FROM posts)
                    SELECT * FROM numbered_posts
                    WHERE user_id IN(""");
            for(int id: user_ids){
                query.append(id).append(",");
            }
            query.setCharAt(query.lastIndexOf(","),')');
            query.append(" AND row_number > ? and row_number <= ?");


            HashMap<Integer,HashMap<Integer,String>> results = new LinkedHashMap<>();
            try(PreparedStatement stm = conn.prepareStatement(query.toString())){
                stm.setInt(1,pageNum*pageSize);
                stm.setInt(2, pageSize*(pageNum+1));

                ResultSet rs = stm.executeQuery();
                while(rs.next()){
                    int user_id = rs.getInt("user_id");

                    int post_id = rs.getInt("post_id");
                    String text = rs.getString("text");
                    if(!results.containsKey(user_id)) {
                        results.put(user_id, new LinkedHashMap<>());
                    }
                    results.get(user_id).put(post_id,text);
                }

            }
            return results;
        };
        try{
            return dbUtils.doInTransaction(getPosts);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException(e.getMessage());
        }
    }

    @Override
    public void registerLink(int user, int post) {
//        DbUtils.ThrowingConsumer<Connection, Exception> insert_link = (conn) -> {
//            String query = "INSERT INTO links SELECT ?,? WHERE NOT EXISTS(SELECT * FROM links WHERE user_id=? AND post_id=?)";
//            try(PreparedStatement pst = conn.prepareStatement(query)){
//                pst.setInt(1,user);
//                pst.setInt(2,post);
//                pst.setInt(3,user);
//                pst.setInt(4,post);
//                pst.executeUpdate();
//            }
//        };
//        try{
//            dbUtils.doInTransaction(insert_link);
//            logger.info("Link info registered successfully");
//        }catch(Exception e){
//            logger.error("Could not save link info");
//            throw new DatabaseException(e.getMessage());
//        }

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        sessionFactory.inTransaction(session -> {
            try{
                UserEntity LinkCreator = session.getReference(UserEntity.class,user);
                PostEntity SharedPost = session.getReference(PostEntity.class, post);
                LinkCreator.addToSharedPosts(SharedPost);
                session.merge(LinkCreator);
            }catch(Exception e){
                System.out.println(e.getStackTrace());
            }

        });

    }

    @Override
    public boolean checkLink(int user, int post) {
        DbUtils.ThrowingFunction<Connection, Boolean, Exception> check_link = (conn) -> {
            String query = "SELECT EXISTS(SELECT * FROM links WHERE user_id=? AND post_id=?)";
            boolean exist;
            try(PreparedStatement pst = conn.prepareStatement(query)){
                pst.setInt(1,user);
                pst.setInt(2,post);
                ResultSet rs = pst.executeQuery();
                rs.next();
                exist = rs.getBoolean("exists");
            }
            return exist;
        };
        try{
            return dbUtils.doInTransaction(check_link);
        }catch(Exception e){
            logger.error("Could not check link");
            throw new DatabaseException(e.getMessage());
        }
    }
}
