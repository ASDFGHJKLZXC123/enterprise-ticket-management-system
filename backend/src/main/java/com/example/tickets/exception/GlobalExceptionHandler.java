package com.example.tickets.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(NotFoundException ex, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler({ForbiddenException.class, AccessDeniedException.class})
    ResponseEntity<ErrorResponse> forbidden(RuntimeException ex, HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler({BadRequestException.class, BadCredentialsException.class})
    ResponseEntity<ErrorResponse> badRequest(RuntimeException ex, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + " " + error.getDefaultMessage())
            .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> generic(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI()
        ));
    }
}

