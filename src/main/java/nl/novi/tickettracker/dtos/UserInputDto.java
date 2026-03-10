package nl.novi.tickettracker.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserInputDto {

    @NotBlank(message = "Username is mandatory")
    public String username;

    @NotBlank(message = "Password is mandatory")
    public String password;

    @NotBlank(message = "First name is mandatory")
    public String firstname;

    @NotBlank(message = "Last name is mandatory")
    public String lastname;

    public String bio;

    @Email(message = "Please provide a valid email address")
    @NotBlank(message = "Email is mandatory")
    public String email;

    public String phone;

    @NotNull(message = "Birthdate is mandatory")
    public LocalDateTime birthdate;
}