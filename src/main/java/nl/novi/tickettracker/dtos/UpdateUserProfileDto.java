package nl.novi.tickettracker.dtos;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateUserProfileDto {
    public String firstname;
    public String lastname;
    public String bio;

    @Email(message = "Please provide a valid email address")
    public String email;

    public String phone;
    public LocalDateTime birthdate;
}