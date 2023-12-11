package org.apostolis.posts.adapter.out.persistence;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.apostolis.common.PageRequest;
import org.apostolis.common.TransactionUtils;
import org.apostolis.exception.DatabaseException;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.Post;
import org.apostolis.posts.domain.PostDTO;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.UserId;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

// Post database crud operations
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {
    private final TransactionUtils transactionUtils;
    private static final Logger logger = LoggerFactory.getLogger(PostRepositoryImpl.class);

    @Override
    public void savePost(Post postToSave) {
        TransactionUtils.ThrowingConsumer<Session,Exception> task = (session) ->
                session.persist(new PostEntity(postToSave.getUser().getUser_id(), postToSave.getText(),postToSave.getCreatedAt()));
        try{
            transactionUtils.doInTransaction(task);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not save new post",e);
        }

    }

    @Override
    public Map<PostId,PostDTO> getPostById(PostId post_id) {
        TransactionUtils.ThrowingFunction<Session, Map<PostId,PostDTO>, Exception> task = (session) -> {
            String postsQuery = """
                        select post_id as pid, text as p_text
                        from PostEntity
                        where post_id = :post""";

            Tuple post = session.createSelectionQuery(postsQuery, Tuple.class)
                    .setParameter("post",post_id.getPost_id())
                    .getSingleResult();
            Map<PostId, PostDTO> postDTOHashMap = new HashMap<>();
            postDTOHashMap.put(new PostId((Long)post.get("pid")),new PostDTO((String)post.get("p_text")));
            return postDTOHashMap;
        };
        try{
            return transactionUtils.doInTransaction(task);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve post by user id",e);
        }
    }

    @Override
    public Map<UserId,Map<PostId,PostDTO>> getPostsGivenUsersIds(List<UserId> user_ids, PageRequest req) {

        TransactionUtils.ThrowingFunction<Session, Map<UserId,Map<PostId,PostDTO>>, Exception> task = (session) -> {
            List<Long> numeric_ids = new ArrayList<>();
            for(UserId id: user_ids){
                numeric_ids.add(id.getUser_id());
            }
            String postsQuery = """
                        select user_id as uid, post_id as pid, text as p_text
                        from PostEntity
                        where user_id in(:user_ids)
                        order by createdAt DESC""";

            List<Tuple> post_tuples = session.createSelectionQuery(postsQuery, Tuple.class)
                    .setParameter("user_ids", numeric_ids)
                    .setFirstResult(req.pageNumber() * req.pageSize())
                    .setMaxResults(req.pageSize())
                    .getResultList();

            Map<UserId, Map<PostId, PostDTO>> results = new LinkedHashMap<>();

            for (Tuple post : post_tuples) {
                UserId user_id = new UserId((Long) post.get("uid"));
                long post_id = (Long) post.get("pid");
                if (!results.containsKey(user_id)) {
                    results.put(user_id, new LinkedHashMap<>());
                }
                results.get(user_id).put(new PostId(post_id), new PostDTO((String) post.get("p_text")));
            }
            return results;
        };
        try {
            return transactionUtils.doInTransaction(task);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve posts by user ids",e);
        }
    }

    @Override
    public void registerLink(PostId post_id) {
        TransactionUtils.ThrowingConsumer<Session, Exception> task = (session) -> {
            PostEntity sharedPost = session.getReference(PostEntity.class, post_id.getPost_id());
            sharedPost.setShared();
            session.merge(sharedPost);
        };
        try {
            transactionUtils.doInTransaction(task);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not save registered link",e);
        }
    }

    @Override
    public boolean checkLink(PostId post_id) {
        TransactionUtils.ThrowingFunction<Session, Boolean, Exception> task = (session) -> {
            String query = "select isShared from PostEntity where post_id= :post";
            return session.createSelectionQuery(query, Boolean.class)
                    .setParameter("post", post_id.getPost_id())
                    .getSingleResult();
        };
        try {
            return transactionUtils.doInTransaction(task);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not find link",e);
        }
    }

    @Override
    public boolean isMyPost(UserId user_id, PostId post_id) {
        TransactionUtils.ThrowingFunction<Session, Boolean, Exception> task = (session) -> {
            String query = """
                        select count(*) as exist
                        from PostEntity
                        where post_id = :post and user_id = :user""";

            long exists = session.createSelectionQuery(query, Long.class)
                    .setParameter("user", user_id.getUser_id())
                    .setParameter("post", post_id.getPost_id())
                    .getSingleResult();
            return exists > 0;
        };
        try {
            return transactionUtils.doInTransaction(task);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not check if post belongs to user",e);
        }
    }
}
