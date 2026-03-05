package nl.novi.tickettracker.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ProjectInputDto {

    @NotBlank(message = "Project name is mandatory")
    public String name;
    public String description;
    public LocalDateTime startDate;
}