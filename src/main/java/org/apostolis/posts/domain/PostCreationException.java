package org.apostolis.posts.domain;

public class PostCreationException extends RuntimeException{

    public PostCreationException(String message){
        super(message);
    }
}
