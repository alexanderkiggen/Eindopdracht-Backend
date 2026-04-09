package nl.novi.tickettracker.dtos;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectUpdateDto {
    public String name;
    public String description;
    public LocalDateTime startDate;
}