package nl.novi.tickettracker.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({"id", "fileName", "contentType"})

public class FileAttachmentOutputDto {
    private Integer id;
    private String fileName;
    private String contentType;
}