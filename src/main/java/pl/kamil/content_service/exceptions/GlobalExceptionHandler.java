package pl.kamil.content_service.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors()
                .forEach(error -> {

                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();

                    errors.put(fieldName, errorMessage);
                });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException e, HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiError> handleFileStorageException(FileStorageException e, HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.BAD_GATEWAY.value(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ApiError> handleFileProcessingException(FileProcessingException e, HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ForbiddenAccessException.class)
    public ResponseEntity<ApiError> handleForbiddenAccessException(ForbiddenAccessException e, HttpServletRequest request) {

        ApiError error = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException e, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // handles many DB Exceptions
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiError> handleDataAccessException(DataAccessException e, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingRequestHeaderException(MissingRequestHeaderException e, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                e.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception e, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Something went wrong",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiError> handleMissingServletRequestPartException(MissingServletRequestPartException e, HttpServletRequest request) {
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "File is missing. Please attach a file under 'file' part.",
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
