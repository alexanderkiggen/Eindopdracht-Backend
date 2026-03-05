package nl.novi.tickettracker.dtos;

import lombok.Getter;
import lombok.Setter;
import nl.novi.tickettracker.models.TicketType;

@Getter
@Setter
public class TicketOutputDto {
    public Integer id;
    public String title;
    public String description;
    public String status;
    public TicketType type;
    public Integer projectId;
}