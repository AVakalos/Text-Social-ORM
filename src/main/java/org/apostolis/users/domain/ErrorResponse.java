package org.apostolis.users.domain;

public class ErrorResponse {
    private final String errorDescription;
    private final String errorCause;


    public ErrorResponse(String errorDescription, String errorCause) {
        this.errorDescription = errorDescription;
        this.errorCause = errorCause;
    }
}
