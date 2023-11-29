package org.apostolis.comments.adapter.out.persistence;

import jakarta.persistence.Tuple;
import org.apostolis.AppConfig;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentDTO;
import org.apostolis.common.PageRequest;
import org.apostolis.exception.DatabaseException;
import org.apostolis.posts.adapter.out.persistence.PostEntity;
import org.apostolis.posts.adapter.out.persistence.PostId;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.apostolis.users.adapter.out.persistence.UserId;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// Comment database CRUD operations
public class CommentRepositoryImpl implements CommentRepository {
    private static final Logger logger = LoggerFactory.getLogger(CommentRepositoryImpl.class);

    @Override
    public void saveComment(Comment commentToSave) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        sessionFactory.inTransaction((session -> {
            UserEntity commentCreator = session.getReference(UserEntity.class, commentToSave.user().getUser_id());
            PostEntity post = session.getReference(PostEntity.class, commentToSave.post());
            session.persist(new CommentEntity(post, commentCreator, commentToSave.text(), commentToSave.createdAt()));
        }));
    }

    @Override
    public long getCountOfUserCommentsUnderThisPost(UserId UserId, PostId post) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        return sessionFactory.fromTransaction(session -> {
            String query = """
                    select count(*) as count
                    from CommentEntity
                    where post.post_id = :post and commentCreator.user_id=:user
                    """;
            return session.createSelectionQuery(query,Long.class)
                    .setParameter("post",post.getPost_id())
                    .setParameter("user",UserId.getUser_id())
                    .getSingleResult();
        });
    }

    @Override
    public Map<PostId, Map<CommentId, CommentDTO>> getCommentsGivenPostIds(List<PostId> post_ids, PageRequest req) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        List<Long> numeric_ids = new ArrayList<>();
        for(PostId pid: post_ids){
            numeric_ids.add(pid.getPost_id());
        }
        try{
            return sessionFactory.fromTransaction(session -> {
                String commentsQuery = """
                        select post.post_id as pid, comment_id as cid, text as c_text
                        from CommentEntity
                        where post.post_id in(:post_ids)
                        order by createdAt DESC""";
                List<Tuple> comment_tuples = session.createQuery(commentsQuery, Tuple.class)
                        .setParameter("post_ids", numeric_ids)
                        .setFirstResult(req.pageNumber() * req.pageSize())
                        .setMaxResults(req.pageSize())
                        .getResultList();

                return processQueryResults(comment_tuples);

            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve comments by post ids",e);
        }
    }

    public Map<PostId, Map<CommentId, CommentDTO>> getLatestCommentsGivenPostIds(List<PostId> post_ids) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        List<Long> numeric_ids = new ArrayList<>();
        for(PostId pid: post_ids){
            numeric_ids.add(pid.getPost_id());
        }
        try{
            return sessionFactory.fromTransaction(session -> {

                String commentsQuery = """
                        select rc.pid as pid, rc.cid as cid, rc.c_text as c_text
                        from (
                            select post.post_id as pid,
                                    comment_id as cid,
                                    text as c_text,
                                    row_number() over (partition by post.post_id) as rowNum
                            from CommentEntity
                            where post.post_id in(:post_ids)
                            order by createdAt DESC) as rc
                        where rc.rowNum = 1""";
                List<Tuple> comment_tuples = session.createQuery(commentsQuery, Tuple.class)
                        .setParameter("post_ids", numeric_ids)
                        .getResultList();

                return processQueryResults(comment_tuples);
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve latest comments by post ids",e);
        }
    }

    @NotNull
    private Map<PostId, Map<CommentId, CommentDTO>> processQueryResults(List<Tuple> comment_tuples) {
        Map<PostId,Map<CommentId,CommentDTO>> results = new LinkedHashMap<>();

        for (Tuple comment : comment_tuples) {
            PostId post_id = new PostId((Long) comment.get("pid"));
            CommentId comment_id = new CommentId((Long) comment.get("cid"));
            if (!results.containsKey(post_id)) {
                results.put(post_id, new LinkedHashMap<>());
            }
            results.get(post_id).put(comment_id, new CommentDTO((String) comment.get("c_text")));
        }
        return results;
    }
}
