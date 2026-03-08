package nl.novi.tickettracker.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignDeveloperDto {
    @NotBlank(message = "Username is mandatory")
    public String username;
}