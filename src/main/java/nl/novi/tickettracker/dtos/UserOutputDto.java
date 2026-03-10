package nl.novi.tickettracker.dtos;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserOutputDto {
    public Integer id;
    public String username;
    public String firstname;
    public String lastname;
    public String bio;
    public String email;
    public String phone;
    public LocalDateTime birthdate;
}