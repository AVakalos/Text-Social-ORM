package org.apostolis.comments.domain;

import org.apostolis.AppConfig;

import java.time.LocalDateTime;

public record Comment(
        long user,
        long post,
        String text,
        LocalDateTime createdAt
){

    public Comment(long user, long post, String text) {
        this(user,post,text,LocalDateTime.now(AppConfig.clock));
    }
}
