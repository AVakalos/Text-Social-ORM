package org.apostolis.posts.domain;

import org.apostolis.AppConfig;
import org.apostolis.users.adapter.out.persistence.UserId;

import java.time.LocalDateTime;

public record Post(
        UserId user,
        String text,
        LocalDateTime createdAt){

    public Post(UserId user, String text){
        this(user,text,LocalDateTime.now(AppConfig.getClock()));
    }
}
