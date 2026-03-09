package nl.novi.tickettracker.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentInputDto {
    @NotBlank(message = "Comment can not be empty and require 'text'.")
    public String text;
}