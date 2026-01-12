package pl.kamil.content_service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private int status;
    private String message;
    private String path;
    Map<String, String> errors;
    private Instant timestamp;

    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
        this.path = "";
        this.timestamp = Instant.now();
    }

    public ApiError(int status, String message, String path) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = Instant.now();
    }

}