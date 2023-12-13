package org.apostolis.comments.domain;

import lombok.Getter;
import org.apostolis.AppConfig;
import org.apostolis.posts.domain.PostId;
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
}
