package org.apostolis.comments.application.service;

import org.apostolis.AppConfig;
import org.apostolis.comments.application.ports.in.CreateCommentCommand;
import org.apostolis.comments.application.ports.in.CreateCommentUseCase;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.posts.adapter.out.persistence.PostId;
import org.apostolis.users.adapter.out.persistence.UserId;
import org.apostolis.users.domain.Role;

// Business logic for comment creation
public class CommentService implements CreateCommentUseCase {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public void createComment(CreateCommentCommand createCommentCommand) throws CommentCreationException {
        Role role = Role.valueOf(createCommentCommand.role());

        if(role.equals(Role.FREE)){
            long comments_count = commentRepository.getCountOfUserCommentsUnderThisPost(
                    new UserId(createCommentCommand.user()), new PostId(createCommentCommand.post()));

            int max_comments_number = AppConfig.getFREE_MAX_COMMENTS();
            if(comments_count >= max_comments_number){
                throw new CommentCreationException("Free users can comment up to "+ max_comments_number +" times per post."+
                        "\nYou reached the maximum number of comments for this post.");
            }
        }
        Comment commentToSave = new Comment(new UserId(createCommentCommand.user()), createCommentCommand.post(), createCommentCommand.text());
        commentRepository.saveComment(commentToSave);
    }
}
