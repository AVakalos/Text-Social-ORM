package org.apostolis.common;

import io.javalin.http.Context;
import jakarta.validation.ConstraintViolationException;
import org.apostolis.users.domain.ErrorResponse;

import java.time.Clock;
public class ErrorController {

    private final Clock clock;

    public ErrorController(Clock clock) {
        this.clock = clock;
    }

//    public ErrorResponse handleConstraintViolation(ConstraintViolationException c, Context ctx){
//
//
//    }
}
