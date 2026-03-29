package nl.novi.tickettracker.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTicketProjectDto {
    @NotNull(message = "Project ID is mandatory")
    public Integer projectId;
}