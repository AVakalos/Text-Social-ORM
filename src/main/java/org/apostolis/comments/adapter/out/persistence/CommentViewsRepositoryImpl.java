package org.apostolis.comments.adapter.out.persistence;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.CommentDetails;
import org.apostolis.comments.domain.CommentId;
import org.apostolis.common.PersistenseDataTypes.CommentsByPostId;
import org.apostolis.common.PageRequest;
import org.apostolis.common.TransactionUtils;
import org.apostolis.exception.DatabaseException;
import org.apostolis.posts.domain.PostId;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// Read only repository for Comment Entity
@RequiredArgsConstructor
public class CommentViewsRepositoryImpl implements CommentRepository {

    private final TransactionUtils transactionUtils;
    private static final Logger logger = LoggerFactory.getLogger(CommentViewsRepositoryImpl.class);

    @Override
    public CommentsByPostId getCommentsGivenPostIds(List<PostId> post_ids, PageRequest req) {
        TransactionUtils.ThrowingFunction<Session, CommentsByPostId, Exception> dbtask = (session) -> {
            List<Long> numeric_ids = new ArrayList<>();
            for(PostId pid: post_ids){
                numeric_ids.add(pid.getPost_id());
                System.out.println(numeric_ids);
            }
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
        };
        try{
            return transactionUtils.doInTransaction(dbtask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve comments by post ids",e);
        }
    }

    public CommentsByPostId getLatestCommentsGivenPostIds(List<PostId> post_ids) {
        TransactionUtils.ThrowingFunction<Session, CommentsByPostId, Exception> dbtask = (session) -> {
            List<Long> numeric_ids = new ArrayList<>();
            for(PostId pid: post_ids){
                numeric_ids.add(pid.getPost_id());
            }
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
        };
        try{
            return transactionUtils.doInTransaction(dbtask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve latest comments by post ids",e);
        }
    }

    private CommentsByPostId processQueryResults(List<Tuple> comment_tuples) {
        Map<PostId,Map<CommentId, CommentDetails>> results = new LinkedHashMap<>();

        for (Tuple comment : comment_tuples) {
            System.out.println(comment);
            PostId post_id = new PostId((Long) comment.get("pid"));
            CommentId comment_id = new CommentId((Long) comment.get("cid"));
            if (!results.containsKey(post_id)) {
                results.put(post_id, new LinkedHashMap<>());
            }
            results.get(post_id).put(comment_id, new CommentDetails((String) comment.get("c_text")));
        }
        return new CommentsByPostId(results);
    }
}
