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
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST); // Status code: 400 Bad Request
    }

    // Foute Enum waardes of onbekende velden
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        String message = "Malformed JSON request. Please check your request body.";

        ex.getMostSpecificCause();
        if (ex.getMostSpecificCause().getMessage() != null) {
            String specificMessage = ex.getMostSpecificCause().getMessage();

            // Controleer Enum fouten
            if (specificMessage.contains("not one of the values accepted for Enum class")) {
                String fieldName = "field";
                if (specificMessage.contains("TicketStatus")) fieldName = "status";
                else if (specificMessage.contains("TicketType")) fieldName = "type";

                int startIndex = specificMessage.indexOf("[");
                int endIndex = specificMessage.indexOf("]") + 1;

                if (startIndex != -1) {
                    String acceptedValues = specificMessage.substring(startIndex, endIndex);
                    message = "Invalid Enum value provided for " + fieldName + ". Accepted values are: " + acceptedValues;
                }
            }
            // Controleer onbekende velden
            else if (specificMessage.toLowerCase().contains("unrecognized")) {
                String[] parts = specificMessage.split("\"");
                if (parts.length > 1) {
                    message = "The field '" + parts[1] + "' is not recognized or not allowed to be updated.";
                } else {
                    message = specificMessage.split(" at \\[Source")[0];
                }
            }
            // Overige
            else {
                message = specificMessage.split(" at \\[Source")[0];
            }
        }

        Map<String, Object> body = createStandardErrorBody(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST); // Status code: 400 Bad Request
    }

    // Bestand te groot
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSizeException(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        String message = "The uploaded file is too large.";
        Map<String, Object> body = createStandardErrorBody(HttpStatus.PAYLOAD_TOO_LARGE, message, request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE); // Status code: 413 Payload Too Large
    }

    // Record niet gevonden
    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRecordNotFoundException(RecordNotFoundException ex, HttpServletRequest request) {
        Map<String, Object> body = createStandardErrorBody(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND); // Status code: 404 Not Found
    }

    // Verkeerd type in URL
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = "Invalid value provided for parameter '" + ex.getName() + "'.";
        Map<String, Object> body = createStandardErrorBody(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST); // Status code: 400 Bad Request
    }

    // Waarde al in gebruik
    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "A record with this unique value already exists.";

        ex.getMostSpecificCause();
        if (ex.getMostSpecificCause().getMessage().contains("duplicate key value")) {
            message = "The provided username or email is already in use. Please choose another one.";
        }

        Map<String, Object> body = createStandardErrorBody(HttpStatus.CONFLICT, message, request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT); // Status code: 409 Conflict
    }

    // Validatiefouten @ModelAttribute
    @ExceptionHandler(org.springframework.validation.BindException.class)
    public ResponseEntity<Map<String, Object>> handleBindExceptions(org.springframework.validation.BindException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        Map<String, Object> body = createStandardErrorBody(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Vangt fout af als @RequestParam("file") ontbreekt
    @ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParts(org.springframework.web.multipart.support.MissingServletRequestPartException ex, HttpServletRequest request) {
        String message = "Missing required file/part: " + ex.getRequestPartName();
        Map<String, Object> body = createStandardErrorBody(HttpStatus.BAD_REQUEST, message, request.getRequestURI());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Vangt ResponseStatusExceptions af bij bestandsupload
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(org.springframework.web.server.ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : "Something went wrong.";

        Map<String, Object> body = createStandardErrorBody(status, message, request.getRequestURI());
        return new ResponseEntity<>(body, status);
    }
}