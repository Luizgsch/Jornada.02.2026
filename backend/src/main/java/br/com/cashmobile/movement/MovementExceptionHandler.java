package br.com.cashmobile.movement;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MovementExceptionHandler {
    @ExceptionHandler(MovementFailure.class)
    public ProblemDetail movementFailure(MovementFailure failure) {
        var status = switch (failure.reason()) {
            case INVALID -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case CAPACITY -> HttpStatus.SERVICE_UNAVAILABLE;
        };
        return ProblemDetail.forStatusAndDetail(status, failure.getMessage());
    }
}
