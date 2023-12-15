package org.apostolis.comments.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.comments.application.ports.in.CommentsViewsUseCase;
import org.apostolis.comments.application.ports.in.ViewCommentsQuery;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.CommentsOnOwnPostsView;
import org.apostolis.comments.domain.LatestCommentOnPostView;
import org.apostolis.common.*;
import org.apostolis.common.PersistenseDataTypes.CommentsByPostId;
import org.apostolis.common.PersistenseDataTypes.PostsById;
import org.apostolis.common.PersistenseDataTypes.PostsByUserId;
import org.apostolis.common.PersistenseDataTypes.UsersById;
import org.apostolis.posts.domain.PostId;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.PostDetails;
import org.apostolis.users.domain.UserId;
import org.apostolis.users.application.ports.out.FollowViewsRepository;
import org.hibernate.Session;



import java.util.*;

// Business logic for comments views
@RequiredArgsConstructor
public class CommentsViewService implements CommentsViewsUseCase {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final FollowViewsRepository followViewsRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public CommentsOnOwnPostsView getCommentsOnOwnPosts(ViewCommentsQuery viewCommentsQuery) throws Exception {
        TransactionUtils.ThrowingFunction<Session, CommentsOnOwnPostsView, Exception> getComments = (session) -> {

            ArrayList<UserId> user_id = new ArrayList<>(List.of(viewCommentsQuery.user_id()));
            Map<PostId,PostDetails> ownPosts = postRepository.getPostsGivenUsersIds(
                    user_id,new PageRequest(0, Integer.MAX_VALUE)).getData().get(viewCommentsQuery.user_id());

            if(ownPosts == null){
                return new CommentsOnOwnPostsView(new PostsById(new HashMap<>()),new CommentsByPostId(new HashMap<>()));
            }
            ArrayList<PostId> post_ids = new ArrayList<>(ownPosts.keySet());

            CommentsByPostId comments = commentRepository.getCommentsGivenPostIds(
                    post_ids,viewCommentsQuery.pageRequest());

            return new CommentsOnOwnPostsView(
                    new PostsById(ownPosts),
                    Objects.requireNonNullElseGet(comments, () -> new CommentsByPostId(new HashMap<>())));

        };
        return transactionUtils.doInTransaction(getComments);
    }

    @Override
    public LatestCommentOnPostView getLatestCommentsOnOwnOrFollowingPosts(ViewCommentsQuery viewCommentsQuery) throws Exception {
        TransactionUtils.ThrowingFunction<Session, LatestCommentOnPostView, Exception> getLatestComments = (session) -> {

            UsersById following_users = followViewsRepository.getFollowing(
                    viewCommentsQuery.user_id(),new PageRequest(0, Integer.MAX_VALUE));

            ArrayList<UserId> user_ids = new ArrayList<>(following_users.getData().keySet());
            user_ids.add(viewCommentsQuery.user_id());

            PostsByUserId own_and_following_posts = postRepository.getPostsGivenUsersIds(
                    user_ids, viewCommentsQuery.pageRequest());

            if(own_and_following_posts == null){
                return new LatestCommentOnPostView(
                        new PostsByUserId(new HashMap<>()), new CommentsByPostId(new HashMap<>()));
            }

            ArrayList<PostId> post_ids = new ArrayList<>();
            for(UserId user: own_and_following_posts.getData().keySet()){
                post_ids.addAll(own_and_following_posts.getData().get(user).keySet());
            }
            CommentsByPostId latest_comments = commentRepository.getLatestCommentsGivenPostIds(post_ids);

            return new LatestCommentOnPostView(
                    new PostsByUserId(own_and_following_posts.getData()),
                    Objects.requireNonNullElseGet(latest_comments, () -> new CommentsByPostId(new HashMap<>())));

        };
        return transactionUtils.doInTransaction(getLatestComments);
    }
}