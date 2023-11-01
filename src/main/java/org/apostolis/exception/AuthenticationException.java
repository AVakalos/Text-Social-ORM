package org.apostolis.exception;

public class AuthenticationException extends RuntimeException{
    public AuthenticationException(String errorMessage, Throwable err){
        super(errorMessage, err);
    }
    public AuthenticationException(String errorMessage){
        super(errorMessage);
    }

}
