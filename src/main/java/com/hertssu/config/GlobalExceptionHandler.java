package com.hertssu.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.hertssu.exceptions.*;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MeetingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleMeetingNotFound(MeetingNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "MEETING_NOT_FOUND", "message", e.getMessage()));
    }

    @ExceptionHandler(ZoomSyncException.class)
    public ResponseEntity<Map<String, String>> handleZoomSync(ZoomSyncException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(Map.of("error", "ZOOM_SYNC_FAILED", "message", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        if ("MICROSOFT_REAUTH_REQUIRED".equals(e.getMessage())) {
            return ResponseEntity.status(401).body(Map.of(
                "error", "MICROSOFT_REAUTH_REQUIRED",
                "message", "Microsoft access expired. Please log in again."
            ));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "INTERNAL_ERROR", "message", "An error occurred"));
    }
}
