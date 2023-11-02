package org.apostolis.posts.adapter.out.persistence;

import org.apostolis.common.DbUtils;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.Post;
import org.apostolis.posts.domain.PostCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

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
}
