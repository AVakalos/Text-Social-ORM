package org.apostolis.posts.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.common.*;
import org.apostolis.common.PersistenseDataTypes.CommentsByPostId;
import org.apostolis.common.PersistenseDataTypes.PostsById;
import org.apostolis.common.PersistenseDataTypes.PostsByUserId;
import org.apostolis.common.PersistenseDataTypes.UsersById;
import org.apostolis.posts.domain.*;
import org.apostolis.posts.application.ports.in.PostViewsUseCase;
import org.apostolis.posts.application.ports.in.PostWithNCommentsQuery;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.application.ports.in.OwnPostsWithNCommentsQuery;
import org.apostolis.posts.application.ports.in.PostViewsQuery;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.application.ports.out.FollowsRepository;
import org.hibernate.Session;

import java.util.*;
import java.util.List;

// Post views business logic
@RequiredArgsConstructor
public class PostViewService implements PostViewsUseCase {
    private final PostRepository postRepository;
    private final FollowsRepository followsRepository;
    private final CommentRepository commentRepository;
    private final TransactionUtils transactionUtils;


    @Override
    public FollowingPostsView getFollowingPosts(PostViewsQuery postViewsQuery) throws Exception {
        TransactionUtils.ThrowingFunction<Session, FollowingPostsView, Exception> getPosts = (session) -> {

            UsersById following_users = followsRepository.getFollowing(postViewsQuery.user(),postViewsQuery.pageRequest());
            if(following_users == null){
                return new FollowingPostsView(new PostsByUserId(new HashMap<>()));
            }
            ArrayList<UserId> listOfFollowingIds = new ArrayList<>(following_users.getData().keySet());

            PostsByUserId following_posts =
                    postRepository.getPostsGivenUsersIds(listOfFollowingIds, new PageRequest(0, Integer.MAX_VALUE));
            if(following_posts == null){
                return new FollowingPostsView(new PostsByUserId(new HashMap<>()));
            }
            return new FollowingPostsView(new PostsByUserId(following_posts.getData()));
        };
        return transactionUtils.doInTransaction(getPosts);
    }

    @Override
    public PostsWithNLatestCommentsView getOwnPostsWithNLatestComments(OwnPostsWithNCommentsQuery viewQuery) throws Exception {
        TransactionUtils.ThrowingFunction<Session, PostsWithNLatestCommentsView, Exception> getPosts = (session) -> {
            UserId user = new UserId(viewQuery.user());

            ArrayList<UserId> user_id = new ArrayList<>(List.of(user));
            Map<PostId,PostDetails> ownPosts =
                    postRepository.getPostsGivenUsersIds(user_id,viewQuery.pageRequest()).getData().get(user);
            if(ownPosts == null){
                return new PostsWithNLatestCommentsView(
                        new PostsById(new HashMap<>()), new CommentsByPostId(new HashMap<>()));
            }
            ArrayList<PostId> listOfPostIds = new ArrayList<>(ownPosts.keySet());
            CommentsByPostId latest_N_comments = commentRepository.getCommentsGivenPostIds(
                    listOfPostIds,new PageRequest(0, viewQuery.commentsNum()));

            if(latest_N_comments == null){
                return new PostsWithNLatestCommentsView(new PostsById(ownPosts),new CommentsByPostId(new HashMap<>()));
            }
            return new PostsWithNLatestCommentsView(
                    new PostsById(ownPosts), new CommentsByPostId(latest_N_comments.getData()));
        };
        return transactionUtils.doInTransaction(getPosts);
    }

    @Override
    public PostsWithNLatestCommentsView getPostWithNLatestComments(PostWithNCommentsQuery viewQuery) throws Exception {
        TransactionUtils.ThrowingFunction<Session, PostsWithNLatestCommentsView, Exception> getPosts = (session) -> {
            PostsById post = postRepository.getPostById(new PostId(viewQuery.post_id()));

            ArrayList<PostId> id = new ArrayList<>(post.getData().keySet());
            CommentsByPostId latest_N_comments =
                    commentRepository.getCommentsGivenPostIds(id,new PageRequest(0, viewQuery.comments_num()));

            return new PostsWithNLatestCommentsView(new PostsById(post.getData()),new CommentsByPostId(latest_N_comments.getData()));
        };
        return transactionUtils.doInTransaction(getPosts);
    }
}
