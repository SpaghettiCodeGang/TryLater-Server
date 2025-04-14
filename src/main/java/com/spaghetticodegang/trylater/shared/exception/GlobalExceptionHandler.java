package com.spaghetticodegang.trylater.shared.exception;

import com.spaghetticodegang.trylater.shared.util.MessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Global exception handler for the application.
 * Catches and handles common exceptions thrown across controllers and returns
 * structured error responses with meaningful messages.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageUtil messageUtil;

    /**
     * Handles custom {@link ValidationException} thrown within business logic.
     *
     * @param ex the validation exception
     * @return a 400 Bad Request response with validation errors
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidationException(ValidationException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", messageUtil.get("exception.validation"),
                "errors", ex.getErrors()
        ));
    }

    /**
     * Handles validation errors triggered by {@code @Valid} annotated parameters.
     *
     * @param ex the exception containing field validation errors
     * @return a 400 Bad Request response with field-specific error messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> Objects.requireNonNull(error.getDefaultMessage()),
                        (msg1, msg2) -> msg1
                ));

        return ResponseEntity.badRequest().body(Map.of(
                "message", messageUtil.get("exception.validation"),
                "errors", errors
        ));
    }

    /**
     * Handles authentication failures caused by incorrect passwords.
     *
     * @param ex the exception thrown when credentials are invalid
     * @return a 400 Bad Request response with a user-friendly error message
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", messageUtil.get("auth.invalid.credentials")
        ));
    }

    /**
     * Handles authentication failures caused by non-existent usernames.
     *
     * @param ex the exception thrown when the user is not found
     * @return a 404 Not Found response with a user-friendly error message
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Object> handleUsernameNotFound(UsernameNotFoundException ex) {
        String messageKey = ex.getMessage();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", messageUtil.get(messageKey)
        ));
    }

    /**
     * Handles authentication failures caused by non-existent contacts.
     *
     * @param ex the exception thrown when the contact is not found
     * @return a 404 Not Found response with a user-friendly error message
     */
    @ExceptionHandler(ContactNotFoundException.class)
    public ResponseEntity<Object> handleContactNotFound(ContactNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", messageUtil.get(ex.getMessage())
        ));
    }

    /**
     * Handles authentication failures caused by non-existent recommendation.
     *
     * @param ex the exception thrown when the recommendation is not found
     * @return a 404 Not Found response with a user-friendly error message
     */
    @ExceptionHandler(RecommendationNotFoundException.class)
    public ResponseEntity<Object> handleRecommendationNotFound(RecommendationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", messageUtil.get(ex.getMessage())
        ));
    }

    /**
     * Handles authentication failures caused by non-existent recommendation assignment.
     *
     * @param ex the exception thrown when the recommendation assignment is not found
     * @return a 404 Not Found response with a user-friendly error message
     */
    @ExceptionHandler(RecommendationAssignmentNotFoundException.class)
    public ResponseEntity<Object> handleRecommendationAssignmentNotFound(RecommendationAssignmentNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", messageUtil.get(ex.getMessage())
        ));
    }

    /**
     * Handles failures caused by exceeding image size upload limit.
     *
     * @param ex the exception thrown when the image size is too big
     * @return a 413 Payload Too Large response with a user-friendly error message
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(Map.of(
                "message", messageUtil.get("image.upload.exceed.max.size")));
    }

    /**
     * Handles failures caused by ioException while handling an image.
     *
     * @param ex the exception thrown when the image handle failed
     * @return a 400 Bad Request response with a user-friendly error message
     */
    @ExceptionHandler(ImageHandleException.class)
    public ResponseEntity<Object> handleImageHandleException(ImageHandleException ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "message", messageUtil.get("exception.image.handle"),
                "errors", ex.getErrors()
        ));
    }


    /**
     * Handles uncaught {@link RuntimeException} instances.
     *
     * @param ex the runtime exception
     * @return a 400 Bad Request response
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "message", messageUtil.get("exception.badrequest")));
    }

    /**
     * Handles all other unhandled exceptions.
     *
     * @param ex the exception
     * @return a 500 Internal Server Error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleOtherExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "message", messageUtil.get("exception.internal")));
    }
}
