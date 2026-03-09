package nl.novi.tickettracker.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@JsonPropertyOrder({"id", "text", "timestamp"})
public class CommentOutputDto {
    public Integer id;
    public String text;
    public LocalDateTime timestamp;
}