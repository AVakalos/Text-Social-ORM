package org.apostolis.comments.domain;

import lombok.Getter;
import org.apostolis.AppConfig;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.Role;
import org.apostolis.users.domain.UserId;

import java.time.LocalDateTime;

@Getter
public class Comment{
    private CommentId id;
    private final UserId user;
    private final PostId post;
    private final String text;
    private final LocalDateTime createdAt;


    public Comment(UserId user, PostId post, String text) {
        this.user = user;
        this.post = post;
        this.text = text;
        this.createdAt = LocalDateTime.now(AppConfig.getClock());
    }

    public void validateCommentByUserRole(Role role, long comments_count){
        if(role.equals(Role.FREE)){
            int max_comments_number = AppConfig.getFREE_MAX_COMMENTS();
            if(comments_count >= max_comments_number){
                throw new CommentCreationException("Free users can comment up to "+ max_comments_number +" times per post."+
                        "\nYou reached the maximum number of comments for this post.");
            }
        }
    }
}
