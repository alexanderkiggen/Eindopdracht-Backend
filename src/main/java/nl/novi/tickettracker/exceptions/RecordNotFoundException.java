package nl.novi.tickettracker.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecordNotFoundException extends RuntimeException {

    public RecordNotFoundException() {
        super("Record niet gevonden in de database.");
    }

    public RecordNotFoundException(String message) {
        super(message);
    }
}