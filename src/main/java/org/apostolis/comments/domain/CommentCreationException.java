package org.apostolis.comments.domain;

public class CommentCreationException extends RuntimeException{

    public CommentCreationException(String message, Throwable e){
        super(message,e);
    }
    public CommentCreationException(String message){
        super(message);
    }
}
