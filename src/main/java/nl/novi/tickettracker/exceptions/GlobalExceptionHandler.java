package nl.novi.tickettracker.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
@ControllerAdvice
public class GlobalExceptionHandler {

    private Map<String, Object> createStandardErrorBody(HttpStatus status, String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return body;
    }

    // Validatiefouten (@NotBlank, @NotNull)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        Map<String, Object> body = createStandardErrorBody(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Foute Enum waardes
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Malformed JSON request. Please check your request body.";

        if (ex.getMostSpecificCause() != null && ex.getMostSpecificCause().getMessage() != null) {
            String specificMessage = ex.getMostSpecificCause().getMessage();

            if (specificMessage.contains("not one of the values accepted for Enum class")) {
                String fieldName = "field";
                if (specificMessage.contains("TicketStatus")) {
                    fieldName = "status";
                } else if (specificMessage.contains("TicketType")) {
                    fieldName = "type";
                }

                int startIndex = specificMessage.indexOf("[");
                int endIndex = specificMessage.indexOf("]") + 1;

                if (startIndex != -1 && endIndex != -1) {
                    String acceptedValues = specificMessage.substring(startIndex, endIndex);
                    message = "Invalid Enum value provided for " + fieldName + ". Accepted values are: " + acceptedValues;
                }
            }
        }

        Map<String, Object> body = createStandardErrorBody(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Bestand te groot
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        String message = "The uploaded file is too large. The server refuses to process it.";
        Map<String, Object> body = createStandardErrorBody(HttpStatus.PAYLOAD_TOO_LARGE, message, request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
    }

    // Record niet gevonden
    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRecordNotFoundException(RecordNotFoundException ex, HttpServletRequest request) {
        Map<String, Object> body = createStandardErrorBody(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // Verkeerd type in URL
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Invalid value provided for parameter '" + ex.getName() + "'. Please provide a valid number.";
        Map<String, Object> body = createStandardErrorBody(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}