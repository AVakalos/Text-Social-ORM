package org.apostolis.comments.application.service;

import io.javalin.http.UnauthorizedResponse;
import org.apostolis.App;
import org.apostolis.AppConfig;
import org.apostolis.comments.application.ports.in.CreateCommentCommand;
import org.apostolis.comments.application.ports.in.CreateCommentUseCase;
import org.apostolis.comments.application.ports.out.CommentRepository;
import org.apostolis.comments.domain.Comment;
import org.apostolis.comments.domain.CommentCreationException;
import org.apostolis.users.domain.Role;

public class CommentService implements CreateCommentUseCase {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public void createComment(CreateCommentCommand createCommentCommand) throws CommentCreationException {
        Role role = Role.valueOf(createCommentCommand.role());

        if(role.equals(Role.FREE)){
            int comments_count = commentRepository.getCountOfUserCommentsUnderThisPost(
                    App.currentUserId.get(), createCommentCommand.post());

            int max_comments_number = AppConfig.getFreeMaxComments();
            if(comments_count >= max_comments_number){
                throw new UnauthorizedResponse("Free users can comment up to "+ max_comments_number +" times per post."+
                        "\nYou reached the maximum number of comments for this post.");
            }
        }
        Comment commentToSave = new Comment(App.currentUserId.get(),createCommentCommand.post(), createCommentCommand.text());
        commentRepository.saveComment(commentToSave);
    }
}
