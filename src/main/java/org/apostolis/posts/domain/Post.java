package org.apostolis.posts.domain;

import org.apostolis.AppConfig;

import java.time.LocalDateTime;

public record Post (
        long user,
        String text,
        LocalDateTime createdAt){

    public Post(long user, String text){
        this(user,text,LocalDateTime.now(AppConfig.clock));
    }
}
