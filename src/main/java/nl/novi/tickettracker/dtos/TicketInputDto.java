package nl.novi.tickettracker.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import nl.novi.tickettracker.models.TicketType;

@Getter
@Setter
public class TicketInputDto {

    @NotBlank(message = "Title is mandatory")
    public String title;

    public String description;

    @NotNull(message = "Ticket type is mandatory")
    public TicketType type;

    @NotNull(message = "Project ID is mandatory")
    public Integer projectId;
}