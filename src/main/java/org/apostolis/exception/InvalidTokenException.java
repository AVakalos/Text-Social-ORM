package org.apostolis.exception;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(String errorMessage, Throwable err){
        super(errorMessage, err);
    }

    public InvalidTokenException(Throwable err){
        super("Token has expired or is invalid", err);
    }
}
