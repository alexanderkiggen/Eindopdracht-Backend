package nl.novi.tickettracker.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import nl.novi.tickettracker.models.TicketStatus;

@Getter
@Setter
public class UpdateTicketStatusDto {
    @NotNull(message = "Status is mandatory")
    public TicketStatus status;
}