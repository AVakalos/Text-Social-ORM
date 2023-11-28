package org.apostolis.posts.adapter.out.persistence;

import jakarta.persistence.Tuple;
import org.apostolis.AppConfig;
import org.apostolis.common.PageRequest;
import org.apostolis.exception.DatabaseException;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.Post;
import org.apostolis.posts.domain.PostDTO;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.apostolis.users.adapter.out.persistence.UserId;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PostRepositoryImpl implements PostRepository {

    private static final Logger logger = LoggerFactory.getLogger(PostRepositoryImpl.class);

    @Override
    public void savePost(Post postToSave) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        sessionFactory.inTransaction((session -> {
            UserEntity postCreator = session.getReference(UserEntity.class, postToSave.user().getUser_id());
            session.persist(new PostEntity(postCreator, postToSave.text(),postToSave.createdAt()));
        }));
    }

    @Override
    public Map<PostId,PostDTO> getPostById(PostId post_id) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        try{
            return sessionFactory.fromTransaction(session -> {
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
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve post by user id",e);
        }
    }

    @Override
    public Map<UserId,Map<PostId,PostDTO>> getPostsGivenUsersIds(List<UserId> user_ids, PageRequest req) {

        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        try {
            List<Long> numeric_ids = new ArrayList<>();
            for(UserId id: user_ids){
                numeric_ids.add(id.getUser_id());
            }
            return sessionFactory.fromTransaction(session -> {
                String postsQuery = """
                        select user.user_id as uid, post_id as pid, text as p_text
                        from PostEntity
                        where user.user_id in(:user_ids)
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
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve posts by user ids",e);
        }
    }

    @Override
    public void registerLink(PostId post_id) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        try {
            sessionFactory.inTransaction(session -> {
                PostEntity sharedPost = session.getReference(PostEntity.class, post_id.getPost_id());
                sharedPost.setShared();
                session.merge(sharedPost);
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not save registered link",e);
        }
    }

    @Override
    public boolean checkLink(PostId post_id) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        try {
            return sessionFactory.fromTransaction(session -> {
                String query = "select isShared from PostEntity where post_id= :post";
                return session.createSelectionQuery(query, Boolean.class)
                        .setParameter("post", post_id.getPost_id())
                        .getSingleResult();
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not find link",e);
        }
    }

    @Override
    public boolean isMyPost(UserId user_id, PostId post_id) {
        SessionFactory sessionFactory = AppConfig.getSessionFactory();
        try {
            return sessionFactory.fromTransaction(session -> {
                String query = """
                        select count(*) as exist
                        from PostEntity
                        where post_id = :post and user.user_id = :user""";

                long exists = session.createSelectionQuery(query, Long.class)
                        .setParameter("user", user_id.getUser_id())
                        .setParameter("post", post_id.getPost_id())
                        .getSingleResult();
                return exists > 0;
            });
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not check if post belongs to user",e);
        }
    }
}
