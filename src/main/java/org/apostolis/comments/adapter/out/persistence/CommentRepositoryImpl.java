package org.apostolis.comments.adapter.out.persistence;

import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.common.DbUtils;
import org.apostolis.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class CommentRepositoryImpl implements CommentRepository {

    private final DbUtils dbUtils;

    private static final Logger logger = LoggerFactory.getLogger(CommentRepositoryImpl.class);

    public CommentRepositoryImpl(DbUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public void saveComment(Comment commentToSave) {
        DbUtils.ThrowingConsumer<Connection,Exception> saveCommentIntoDb = (conn) -> {
            try(PreparedStatement savecomment_stm = conn.prepareStatement(
                    "INSERT INTO comments (post_id, user_id, text, created) VALUES (?,?,?,?)")){
                savecomment_stm.setInt(1,commentToSave.post());
                savecomment_stm.setInt(2,commentToSave.user());
                savecomment_stm.setString(3, commentToSave.text());
                savecomment_stm.setTimestamp(4, Timestamp.valueOf(commentToSave.createdAt()));
                savecomment_stm.executeUpdate();
            }
        };
        try{
            dbUtils.doInTransaction(saveCommentIntoDb);
            logger.info("Comment saved successfully in the database.");
        }catch (Exception e){
            logger.error("Comment didn't saved.");
            throw new CommentCreationException("Comment didn't saved. May post id is invalid.");
        }
    }

    @Override
    public int getCountOfUserCommentsUnderThisPost(int user, int post) {
        int comments_count;
        DbUtils.ThrowingFunction<Connection, Integer, Exception> get_comments_count = (conn) -> {
            int count;
            try(PreparedStatement stm = conn.prepareStatement(
                    "SELECT COUNT(*) FROM comments WHERE post_id=? and user_id=?")){
                stm.setInt(1, post);
                stm.setInt(2, user);
                ResultSet rs = stm.executeQuery();
                rs.next();
                count = rs.getInt("count");
            }
            return count;
        };
        try{
            comments_count = dbUtils.doInTransaction(get_comments_count);
            logger.info(String.valueOf(comments_count));
        }catch(Exception e){
            logger.error("Could not retrieve the comments count from database");
            throw new CommentCreationException("Could not retrieve the comments count from database. Invalid post id",e);
        }
        return comments_count;
    }

    @Override
    public HashMap<Integer, HashMap<Integer,String>> getCommentsGivenPostIds(ArrayList<Integer> post_ids, int pageNum, int pageSize) {

        DbUtils.ThrowingFunction<Connection, HashMap<Integer,HashMap<Integer,String>>, Exception> getComments = (conn)->{

            StringBuilder query = new StringBuilder("""
                    WITH numbered_comments AS(
                        SELECT *, row_number() over (
                            partition by post_id
                            ORDER BY created DESC
                        ) AS row_number
                    FROM comments)
                    SELECT * FROM numbered_comments
                    WHERE post_id IN(""");
            for(int id: post_ids){
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
                    int id = rs.getInt("post_id");
                    int comment_id = rs.getInt("comment_id");
                    String text = rs.getString("text");
                    if(!results.containsKey(id)) {
                        results.put(id, new LinkedHashMap<>());
                    }
                    results.get(id).put(comment_id,text);
                }
            }
            return results;
        };
        try{
            return dbUtils.doInTransaction(getComments);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException(e.getMessage());
        }
    }


//    @Override
//    public HashMap<Integer, String> getLatestCommentsGivenPostIds(ArrayList<Integer> post_ids) {
//        DbUtils.ThrowingFunction<Connection, HashMap<Integer,String>, Exception> getComments = (conn)->{
//            StringBuilder query = new StringBuilder(
//                    "WITH numbered_comments AS (\n" +
//                    "    SELECT *, row_number() over (\n" +
//                    "                 PARTITION BY post_id\n" +
//                    "                 order by created DESC\n" +
//                    "                 ) AS row_number\n" +
//                    "    FROM comments\n" +
//                    "    ),\n" +
//                    "    last_comments AS (\n" +
//                    "        SELECT *\n" +
//                    "        FROM numbered_comments\n" +
//                    "        WHERE numbered_comments.row_number = 1\n" +
//                    "    )\n" +
//                    "SELECT comment_id, post_id, text, created\n" +
//                    "FROM last_comments WHERE post_id IN(");
//            for(int id: post_ids){
//                query.append(id).append(",");
//            }
//            query.setCharAt(query.lastIndexOf(","),')');
//            HashMap<Integer,String> results = new HashMap<>();
//            try(PreparedStatement stm = conn.prepareStatement(query.toString())){
//                ResultSet rs = stm.executeQuery();
//                while(rs.next()){
//                    int id = rs.getInt("post_id");
//                    String text = rs.getString("text");
//                    results.put(id, text);
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
//    }


}
