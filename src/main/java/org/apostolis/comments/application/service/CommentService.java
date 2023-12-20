package org.apostolis.comments.application.service;

import lombok.RequiredArgsConstructor;
import org.apostolis.comments.application.ports.in.CreateCommentCommand;
import org.apostolis.comments.application.ports.in.CreateCommentUseCase;
import org.apostolis.comments.domain.Comment;
import org.apostolis.common.TransactionUtils;
import org.apostolis.posts.application.ports.out.PostRepository;
import org.apostolis.posts.domain.Post;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.Role;
import org.apostolis.users.domain.UserId;
import org.hibernate.Session;

import java.util.Set;

// Business logic for comment crud operations
@RequiredArgsConstructor
public class CommentService implements CreateCommentUseCase {

    private final PostRepository postRepository;
    private final TransactionUtils transactionUtils;

    @Override
    public void createComment(CreateCommentCommand createCommentCommand) throws Exception {
        TransactionUtils.ThrowingConsumer<Session,Exception> createComment = (session) -> {
            PostId post_id = createCommentCommand.post();
            UserId user_id = createCommentCommand.user();
            // Find the aggregate root
            Post targetPost = postRepository.fetchPostWithComments(post_id);

            //System.out.println("Domain "+targetPost.getPost_comments().size());

            // Prepare for calling business logic
            Role role = Role.valueOf(createCommentCommand.role());
            long comments_count = 0;
            if (role == Role.FREE){
                comments_count = postRepository.getCountOfUserCommentsUnderThisPost(user_id,post_id);
            }
            // Call business logic from domain
            Comment newComment = new Comment(user_id, post_id, createCommentCommand.text());
            targetPost.addComment(newComment,comments_count,role);

            // Persist new comment from aggregate root
            postRepository.updatePostComments(post_id, newComment);  //targetPost
        };
        transactionUtils.doInTransaction(createComment);
    }
}
