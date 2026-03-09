package nl.novi.tickettracker.dtos;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonPropertyOrder({"id", "name", "description", "startDate"})
public class ProjectOutputDto {
    public Integer id;
    public String name;
    public String description;
    public LocalDateTime startDate;
}