package nl.novi.tickettracker.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import nl.novi.tickettracker.models.TicketType;
import nl.novi.tickettracker.models.TicketStatus;

@Getter
@Setter
public class TicketInputDto {

    @NotBlank(message = "Title is mandatory")
    public String title;

    public String description;

    @NotBlank(message = "Assigned user is mandatory")
    public String assignedUsername;

    @NotNull(message = "Ticket type is mandatory")
    public TicketType type;

    @NotNull(message = "Ticket status is mandatory")
    public TicketStatus status;

    @NotNull(message = "Project ID is mandatory")
    public Integer projectId;
}