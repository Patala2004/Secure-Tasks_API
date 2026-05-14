package com.indramind.cybersec.secure_tasks_api.exceptions;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.indramind.cybersec.secure_tasks_api.logging.CustomLogger;
import com.indramind.cybersec.secure_tasks_api.logging.impl.CustomLoggerFactory;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final CustomLogger log = CustomLoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {

        log.warn("Resource not found: message={}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(LocalDateTime.now(), ex.getMessage())
        );
    }

    @ExceptionHandler(EmailInUseException.class)
    public ResponseEntity<ErrorResponse> handleEmailInUse(EmailInUseException ex){

        log.warn("Email already in use: message={}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ErrorResponse(LocalDateTime.now(), ex.getMessage())
        );
    }

    @ExceptionHandler(CollaboratorAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCollaboratorAlreadyExists(CollaboratorAlreadyExistsException ex){

        log.warn("Collaborator already exists: message={}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ErrorResponse(LocalDateTime.now(), ex.getMessage())
        );
    }

    @ExceptionHandler(CollaboratorNotFound.class)
    public ResponseEntity<ErrorResponse> handleCollaboratorAlreadyExists(CollaboratorNotFound ex){

        log.warn("Collaborator not found: message={}",ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(LocalDateTime.now(), ex.getMessage())
        );
    }

    // Incorrect requests catched by @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {

        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(e -> "[" + e.getField() + "] " + e.getDefaultMessage())
            .reduce((a, b) -> a + " | " + b)
            .orElse("Validation error");
        
        log.warn("Validation failed: errors={}. IP: {}", errorMessage, request.getRemoteAddr());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(LocalDateTime.now(), errorMessage)
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        log.warn("Missing request header: {}", ex, ex.getHeaderName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(LocalDateTime.now(), "Missing required header: " + ex.getHeaderName())
        );
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthz(AuthorizationDeniedException ex){
        log.error("Authorization Error", ex);
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                new ErrorResponse(LocalDateTime.now(), "Unauthorized access")
            );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handle404(NoResourceFoundException ex) {
        log.error("Endpoint not found: {}", ex, ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(LocalDateTime.now(), "Not found")
            );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        Set<HttpMethod> supportedMethods = ex.getSupportedHttpMethods();
        String supportedMethodsString = supportedMethods != null ? 
            supportedMethods.stream().map(HttpMethod::name).collect(Collectors.joining(",")) :
            "None";
        log.error("Method {} not allowed at endpoint {}. Allowed methods: {}.", ex, ex.getMethod(), request.getRequestURI(), supportedMethodsString);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                new ErrorResponse(LocalDateTime.now(), "Method not allowed")
            );
    }

	@ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {

        log.error("Unhandled exception", ex);


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(LocalDateTime.now(), "Internal server error")
        );
    }
}