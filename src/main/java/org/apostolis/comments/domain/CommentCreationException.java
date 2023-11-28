package org.apostolis.comments.domain;

public class CommentCreationException extends RuntimeException{
    public CommentCreationException(String message){
        super(message);
    }
}
