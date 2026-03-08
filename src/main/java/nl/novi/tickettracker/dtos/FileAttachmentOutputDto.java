package nl.novi.tickettracker.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileAttachmentOutputDto {
    private Integer id;
    private String fileName;
    private String contentType;
}