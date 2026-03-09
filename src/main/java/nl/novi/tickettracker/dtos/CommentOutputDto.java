package nl.novi.tickettracker.dtos;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CommentOutputDto {
    public Integer id;
    public String text;
    public LocalDateTime timestamp;
}