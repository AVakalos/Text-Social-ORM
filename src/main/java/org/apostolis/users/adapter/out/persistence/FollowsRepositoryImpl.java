package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.apostolis.common.PageRequest;
import org.apostolis.common.TransactionUtils;
import org.apostolis.common.PersistenseDataTypes.UsersById;
import org.apostolis.exception.DatabaseException;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.apostolis.users.domain.UserDetails;
import org.apostolis.users.domain.UserId;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class FollowsRepositoryImpl implements FollowsRepository {
    private final TransactionUtils transactionUtils;

    private static final Logger logger = LoggerFactory.getLogger(FollowsRepositoryImpl.class);

    @Override
    public void saveFollow(UserId user, UserId user_to_follow) throws IllegalArgumentException, DatabaseException{
        TransactionUtils.ThrowingConsumer<Session,Exception> saveFollowTask = (session) -> {
            UserEntity following_user = session.get(UserEntity.class, user_to_follow.getUser_id());

            FollowerEntityId f_id = new FollowerEntityId(user.getUser_id(), user_to_follow.getUser_id());
            FollowerEntity isPresent = session.get(FollowerEntity.class,f_id);

            if (isPresent != null || following_user == null) {
                throw new IllegalArgumentException("You already follow this user or the user does not exist");
            }

            FollowerEntity newFollow = new FollowerEntity(f_id);
            session.persist(newFollow);
        };
        try {
            transactionUtils.doInTransaction(saveFollowTask);
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not save follow relationship:\n"+e.getMessage(),e);
        }
    }

    @Override
    public void deleteFollow(UserId user, UserId userToUnfollow) throws IllegalArgumentException, DatabaseException {
        TransactionUtils.ThrowingConsumer<Session,Exception> deleteFollowTask = (session) -> {
            UserEntity following_user = session.get(UserEntity.class, userToUnfollow.getUser_id());
            FollowerEntityId f_id = new FollowerEntityId(user.getUser_id(), userToUnfollow.getUser_id());
            FollowerEntity isPresent = session.get(FollowerEntity.class,f_id);

            if (isPresent == null || following_user == null) {
                throw new IllegalArgumentException("You were not following this user or user does not exist");
            }
            session.remove(isPresent);
        };
        try {
            transactionUtils.doInTransaction(deleteFollowTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not delete follow relationship:\n"+e.getMessage(),e);
        }
    }

    @Override
    public UsersById getFollowers(UserId user_id, PageRequest req) throws DatabaseException {
        TransactionUtils.ThrowingFunction<Session, UsersById, Exception> getFollowersTask = (session) -> {
            String followersQuery = """
                select u.user_id as uid, u.username as username
                from UserEntity u join FollowerEntity f on u.user_id = f.id.user_id
                where f.id.following_id = :userId""";
            return processQueryResults(user_id, req, session, followersQuery);
        };
        try {
            return transactionUtils.doInTransaction(getFollowersTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve followers",e);
        }
    }

    @Override
    public UsersById getFollowing(UserId user_id, PageRequest req) throws DatabaseException {
        TransactionUtils.ThrowingFunction<Session, UsersById, Exception> getFollowingTask = (session) -> {
            String followingQuery = """
                select u.user_id as uid, u.username as username
                from UserEntity u join FollowerEntity f on u.user_id = f.id.following_id
                where f.id.user_id = :userId""";
            return processQueryResults(user_id, req, session, followingQuery);
        };
        try {
            return transactionUtils.doInTransaction(getFollowingTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve following users",e);
        }
    }

    @Override
    public UsersById getUsersToFollow(UserId user_id, PageRequest req) throws DatabaseException {
        TransactionUtils.ThrowingFunction<Session, UsersById, Exception> getUsersToFollowTask = (session) -> {
            String usersToFollowQuery = """
                    select user_id as uid, username as username
                    from UserEntity
                    where user_id != :userId and user_id not in(
                        select u.user_id
                        from UserEntity u join FollowerEntity f on u.user_id = f.id.following_id
                        where f.id.user_id = :userId)""";

            return processQueryResults(user_id, req, session, usersToFollowQuery);
        };
        try {
            return transactionUtils.doInTransaction(getUsersToFollowTask);
        }catch(Exception e){
            logger.error(e.getMessage());
            throw new DatabaseException("Could not retrieve users to follow",e);
        }
    }

    private UsersById processQueryResults(UserId user_id, PageRequest req, Session session, String followersQuery) {
        List<Tuple> followers = session.createSelectionQuery(followersQuery, Tuple.class)
                .setParameter("userId", user_id.getUser_id())
                .setFirstResult(req.pageNumber() * req.pageSize())
                .setMaxResults(req.pageSize())
                .getResultList();

        Map<UserId, UserDetails> results = new HashMap<>();
        for (Tuple follower : followers) {
            results.put(new UserId((Long)follower.get("uid")), new UserDetails((String)follower.get("username")));
        }
        return new UsersById(results);
    }
}
