package org.apostolis.posts.adapter.out.persistence;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.apostolis.comments.adapter.out.persistence.CommentEntity;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.common.PageRequest;
import org.apostolis.common.PersistenseDataTypes.PostsById;
import org.apostolis.common.PersistenseDataTypes.PostsByUserId;
import org.apostolis.common.TransactionUtils;
import org.apostolis.exception.DatabaseException;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.*;
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
            session.persist(PostEntity.mapToEntity(postToSave));
        try{
            transactionUtils.doInTransaction(task);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not save new post",e);
        }
    }

    @Override
    public Post fetchPostWithComments(PostId post_id) {
        TransactionUtils.ThrowingFunction<Session, Post, Exception> task = (session) -> {
            PostEntity postEntity = session.find(PostEntity.class, post_id.getValue());
            if(postEntity == null){
                throw new CommentCreationException("Couldn't find the post with id: "+post_id.getValue());
            }
            Set<CommentEntity> commentsEntities = postEntity.getPost_comments();
            Set<Comment> comments = new HashSet<>();
            for(CommentEntity c: commentsEntities){
                comments.add(c.mapToDomain());
            }
            return postEntity.mapToDomain(comments);
        };
        try{
            return transactionUtils.doInTransaction(task);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve post by user id",e);
        }
    }

    @Override
    public void updatePostComments(PostId post_id, Comment newComment) {
        TransactionUtils.ThrowingConsumer<Session,Exception> task = (session) -> {
            PostEntity postEntity = session.find(PostEntity.class, post_id.getValue());
            if(postEntity == null){
                throw new CommentCreationException("Couldn't find the post with id: "+post_id.getValue());
            }
            postEntity.addComment(newComment);

//            Set<Comment> comments = postToUpdate.getPost_comments();
//            for(Comment comment: comments) {
//                postEntity.addComment(CommentEntity.mapToEntity(comment,postEntity));
//            }
            session.persist(postEntity);
        };
        try{
            transactionUtils.doInTransaction(task);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could save new comment to post",e);
        }
    }

    @Override
    public PostsById getPostDetailsById(PostId post_id) {
        TransactionUtils.ThrowingFunction<Session, PostsById, Exception> task = (session) -> {
            String postsQuery = """
                        select post_id as pid, text as p_text
                        from PostEntity
                        where post_id = :post""";

            Tuple post = session.createSelectionQuery(postsQuery, Tuple.class)
                    .setParameter("post",post_id.getValue())
                    .getSingleResult();
            Map<PostId, PostDetails> postHashMap = new HashMap<>();
            postHashMap.put(new PostId((Long)post.get("pid")),new PostDetails((String)post.get("p_text")));
            return new PostsById(postHashMap);
        };
        try{
            return transactionUtils.doInTransaction(task);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve post details by user id",e);
        }
    }

    @Override
    public PostsByUserId getPostsGivenUsersIds(List<UserId> user_ids, PageRequest req) {

        TransactionUtils.ThrowingFunction<Session, PostsByUserId, Exception> task = (session) -> {
            List<Long> numeric_ids = new ArrayList<>();
            for(UserId id: user_ids){
                numeric_ids.add(id.getValue());
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

            Map<UserId, Map<PostId,PostDetails>> results = new LinkedHashMap<>();

            for (Tuple post : post_tuples) {
                UserId user_id = new UserId((Long) post.get("uid"));
                long post_id = (Long) post.get("pid");
                if (!results.containsKey(user_id)) {
                    results.put(user_id, new LinkedHashMap<>());
                }
                results.get(user_id).put(new PostId(post_id), new PostDetails((String) post.get("p_text")));
            }
            return new PostsByUserId(results);
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
            PostEntity sharedPost = session.getReference(PostEntity.class, post_id.getValue());
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
                    .setParameter("post", post_id.getValue())
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
                    .setParameter("user", user_id.getValue())
                    .setParameter("post", post_id.getValue())
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

    @Override
    public long getCountOfUserCommentsUnderThisPost(UserId user, PostId post) {
        TransactionUtils.ThrowingFunction<Session, Long, Exception> getCountTask = (session) -> {
            String query = """
                    select count(*) as count
                    from CommentEntity
                    where post.post_id=:post and commentCreator=:user
                    """;
            return session.createSelectionQuery(query,Long.class)
                    .setParameter("post",post.getValue())
                    .setParameter("user",user.getValue())
                    .getSingleResult();
        };
        try{
            return transactionUtils.doInTransaction(getCountTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could get count of user comments under post",e);
        }
    }
}
