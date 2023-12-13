package org.apostolis.exception;

public class DatabaseException extends RuntimeException{
    public DatabaseException(String message, Throwable err){
        super(message, err);
    }

}
