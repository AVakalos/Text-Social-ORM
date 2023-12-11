package org.apostolis.comments.adapter.out.persistence;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentDTO;
import org.apostolis.comments.domain.CommentId;
import org.apostolis.common.PageRequest;
import org.apostolis.common.TransactionUtils;
import org.apostolis.exception.DatabaseException;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.UserId;
import org.hibernate.Session;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// Comment database CRUD operations
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

    private final TransactionUtils transactionUtils;
    private static final Logger logger = LoggerFactory.getLogger(CommentRepositoryImpl.class);

    @Override
    public void saveComment(Comment commentToSave) {
        TransactionUtils.ThrowingConsumer<Session,Exception> saveCommentTask = (session) ->
            session.persist(
                    new CommentEntity(
                        commentToSave.getPost().getPost_id(),
                        commentToSave.getUser().getUser_id(),
                        commentToSave.getText(),
                        commentToSave.getCreatedAt()));
        System.out.println("Hey");
        try{
            transactionUtils.doInTransaction(saveCommentTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not save new comment",e);
        }
    }

    @Override
    public long getCountOfUserCommentsUnderThisPost(UserId user, PostId post) {
        TransactionUtils.ThrowingFunction<Session, Long, Exception> getCountTask = (session) -> {
            String query = """
                    select count(*) as count
                    from CommentEntity
                    where post_id=:post and commentCreator=:user
                    """;
            return session.createSelectionQuery(query,Long.class)
                    .setParameter("post",post.getPost_id())
                    .setParameter("user",user.getUser_id())
                    .getSingleResult();
        };
        try{
            return transactionUtils.doInTransaction(getCountTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could get count of user comments under post",e);
        }
    }

    @Override
    public Map<PostId, Map<CommentId, CommentDTO>> getCommentsGivenPostIds(List<PostId> post_ids, PageRequest req) {
        TransactionUtils.ThrowingFunction<Session, Map<PostId, Map<CommentId, CommentDTO>>, Exception> dbtask = (session) -> {
            List<Long> numeric_ids = new ArrayList<>();
            for(PostId pid: post_ids){
                numeric_ids.add(pid.getPost_id());
            }
            String commentsQuery = """
                        select post_id as pid, comment_id as cid, text as c_text
                        from CommentEntity
                        where post_id in(:post_ids)
                        order by createdAt DESC""";
            List<Tuple> comment_tuples = session.createQuery(commentsQuery, Tuple.class)
                    .setParameter("post_ids", numeric_ids)
                    .setFirstResult(req.pageNumber() * req.pageSize())
                    .setMaxResults(req.pageSize())
                    .getResultList();

            return processQueryResults(comment_tuples);
        };
        try{
            return transactionUtils.doInTransaction(dbtask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve comments by post ids",e);
        }
    }

    public Map<PostId, Map<CommentId, CommentDTO>> getLatestCommentsGivenPostIds(List<PostId> post_ids) {
        TransactionUtils.ThrowingFunction<Session, Map<PostId, Map<CommentId, CommentDTO>>, Exception> dbtask = (session) -> {
            List<Long> numeric_ids = new ArrayList<>();
            for(PostId pid: post_ids){
                numeric_ids.add(pid.getPost_id());
            }
            String commentsQuery = """
                        select rc.pid as pid, rc.cid as cid, rc.c_text as c_text
                        from (
                            select post_id as pid,
                                    comment_id as cid,
                                    text as c_text,
                                    row_number() over (partition by post_id) as rowNum
                            from CommentEntity
                            where post_id in(:post_ids)
                            order by createdAt DESC) as rc
                        where rc.rowNum = 1""";
            List<Tuple> comment_tuples = session.createQuery(commentsQuery, Tuple.class)
                    .setParameter("post_ids", numeric_ids)
                    .getResultList();

            return processQueryResults(comment_tuples);
        };
        try{
            return transactionUtils.doInTransaction(dbtask);
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
