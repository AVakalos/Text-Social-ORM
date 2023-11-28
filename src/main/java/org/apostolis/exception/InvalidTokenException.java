package org.apostolis.exception;

public class InvalidTokenException extends RuntimeException{
    public InvalidTokenException(Throwable err){
        super("Token has expired or is invalid", err);
    }
}
