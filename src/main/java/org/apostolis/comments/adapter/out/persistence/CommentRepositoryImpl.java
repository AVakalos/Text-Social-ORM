package org.apostolis.comments.adapter.out.persistence;

import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.common.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

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
}
