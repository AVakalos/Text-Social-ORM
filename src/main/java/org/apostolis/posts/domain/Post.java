package org.apostolis.posts.domain;

import lombok.Getter;
import org.apostolis.AppConfig;
import org.apostolis.users.domain.Role;
import org.apostolis.users.domain.UserId;

import java.time.LocalDateTime;

@Getter
public class Post{
    private PostId id;
    private final UserId user;
    private final String text;
    private final LocalDateTime createdAt;

    public Post(UserId user, String text){
        this.user = user;
        this.text = text;
        this.createdAt = LocalDateTime.now(AppConfig.getClock());
    }

    public void validatePostByUserRole(Role role){
        long post_size = text.length();
        switch (role){
            case FREE:
                int free_post_size = AppConfig.getFREE_POST_SIZE();
                if(post_size > free_post_size){
                    throw new PostCreationException("Free users can post texts up to "+ free_post_size +" characters."+
                            "\nYour post was "+post_size+" characters");
                }
                break;
            case PREMIUM:
                int premium_post_size = AppConfig.getPREMIUM_POST_SIZE();
                if(post_size > premium_post_size){
                    throw new PostCreationException("Premium users can post texts up to "+ premium_post_size +" characters."+
                            "\nYour post was "+post_size+" characters");
                }
                break;
            default:
                break;
        }
    }
}
