package org.apostolis.comments.domain;

import org.apostolis.AppConfig;
import org.apostolis.users.adapter.out.persistence.UserId;

import java.time.LocalDateTime;

public record Comment(
        UserId user,
        long post,
        String text,
        LocalDateTime createdAt
){

    public Comment(UserId user, long post, String text) {
        this(user,post,text,LocalDateTime.now(AppConfig.getClock()));
    }
}
