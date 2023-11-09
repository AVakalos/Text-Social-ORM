package org.apostolis.comments.domain;

import org.apostolis.AppConfig;

import java.time.LocalDateTime;

public record Comment(
        int user,
        int post,
        String text,
        LocalDateTime createdAt
){

    public Comment(int user, int post, String text) {
        this(user,post,text,LocalDateTime.now(AppConfig.clock));
    }
}
