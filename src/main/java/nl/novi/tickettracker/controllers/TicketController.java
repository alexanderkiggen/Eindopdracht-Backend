package nl.novi.tickettracker.controllers;

import jakarta.validation.Valid;
import nl.novi.tickettracker.dtos.TicketInputDto;
import nl.novi.tickettracker.dtos.TicketOutputDto;
import nl.novi.tickettracker.services.TicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketOutputDto> createTicket(@Valid @RequestBody TicketInputDto ticketInputDto) {
        TicketOutputDto ticketOutputDto = ticketService.createTicket(ticketInputDto);
        return new ResponseEntity<>(ticketOutputDto, HttpStatus.CREATED); // Status code: 201 Created
    }
}