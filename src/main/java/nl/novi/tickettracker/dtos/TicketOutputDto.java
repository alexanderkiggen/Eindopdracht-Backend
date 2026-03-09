package nl.novi.tickettracker.dtos;

import lombok.Getter;
import lombok.Setter;
import nl.novi.tickettracker.models.TicketType;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@Getter
@Setter
@JsonPropertyOrder({"id", "title", "description", "status", "type", "projectId", "assignedUsername", "files", "comments"})
public class TicketOutputDto {
    public Integer id;
    public String title;
    public String description;
    public String status;
    public TicketType type;
    public Integer projectId;
    public String assignedUsername;
    public List<FileAttachmentOutputDto> files;
    public List<CommentOutputDto> comments;
}