package org.apostolis.posts.adapter.out.persistence;

import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.common.DbUtils;
import org.apostolis.exception.DatabaseException;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.Post;
import org.apostolis.posts.domain.PostCreationException;
import org.apostolis.users.application.ports.out.FollowsRepository;
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
        DbUtils.ThrowingConsumer<Connection,Exception> savePostIntoDb = (conn) -> {
            try(PreparedStatement savepost_stm = conn.prepareStatement(
                    "INSERT INTO posts (user_id, text, created) VALUES (?,?,?)")){
                savepost_stm.setInt(1,postToSave.user());
                savepost_stm.setString(2, postToSave.text());
                savepost_stm.setTimestamp(3, Timestamp.valueOf(postToSave.createdAt()));
                savepost_stm.executeUpdate();
            }
        };
        try{
            dbUtils.doInTransaction(savePostIntoDb);
            logger.info("Post saved successfully in the database.");
        }catch (Exception e){
            logger.error("Post didn't saved.");
            throw new PostCreationException(e.getMessage(),e);
        }
    }

    @Override
    public HashMap<Integer, HashMap<Integer,String>> getPostsGivenUsersIds(ArrayList<Integer> user_ids, int pageNum, int pageSize) {
        DbUtils.ThrowingFunction<Connection, HashMap<Integer,HashMap<Integer,String>>, Exception> getPosts = (conn) -> {


            StringBuilder query = new StringBuilder("""
                    WITH numbered_posts AS(
                        SELECT *, row_number() over (
                            partition by user_id
                            ORDER BY created DESC
                        ) AS row_number
                    FROM posts)
                    SELECT * FROM numbered_posts
                    WHERE user_id IN(""");
            for(int id: user_ids){
                query.append(id).append(",");
            }
            query.setCharAt(query.lastIndexOf(","),')');
            query.append("AND row_number > ? and row_number <= ?");


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
}
