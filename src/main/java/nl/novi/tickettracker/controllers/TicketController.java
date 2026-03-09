package nl.novi.tickettracker.controllers;

import jakarta.validation.Valid;
import nl.novi.tickettracker.dtos.AssignDeveloperDto;
import nl.novi.tickettracker.dtos.TicketInputDto;
import nl.novi.tickettracker.dtos.TicketOutputDto;
import nl.novi.tickettracker.dtos.FileAttachmentOutputDto;
import nl.novi.tickettracker.dtos.CommentOutputDto;
import nl.novi.tickettracker.services.TicketService;
import nl.novi.tickettracker.models.FileAttachment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketOutputDto> createTicket(
            @RequestPart("ticket") @Valid TicketInputDto ticketInputDto,
            @RequestPart("file") MultipartFile file) throws IOException {

        TicketOutputDto ticketOutputDto = ticketService.createTicket(ticketInputDto, file);
        return new ResponseEntity<>(ticketOutputDto, HttpStatus.CREATED); // Status code: 201 Created
    }

    @PutMapping("/{id}/assign")
    public ResponseEntity<TicketOutputDto> assignDeveloperToTicket(
            @PathVariable("id") Integer id,
            @Valid @RequestBody AssignDeveloperDto assignDeveloperDto) {

        TicketOutputDto ticketOutputDto = ticketService.assignDeveloperToTicket(id, assignDeveloperDto.getUsername());
        return new ResponseEntity<>(ticketOutputDto, HttpStatus.OK); // Status code: 200 Ok
    }

    @PostMapping("/{id}/attachments")
    public ResponseEntity<FileAttachmentOutputDto> uploadFile(
            @PathVariable("id") Integer ticketId,
            @RequestParam("file") MultipartFile file) throws IOException {

        FileAttachmentOutputDto dto = ticketService.uploadFileToTicket(ticketId, file);
        return new ResponseEntity<>(dto, HttpStatus.CREATED); // Status code: 201 Created
    }

    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("attachmentId") Integer attachmentId) {
        FileAttachment fileAttachment = ticketService.downloadFile(attachmentId);

        return ResponseEntity.ok() // Status code: 200 Ok
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileAttachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(fileAttachment.getContentType()))
                .body(fileAttachment.getData());
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentOutputDto> addComment(
            @PathVariable("id") Integer ticketId,
            @Valid @RequestBody nl.novi.tickettracker.dtos.CommentInputDto commentInputDto) {

        nl.novi.tickettracker.dtos.CommentOutputDto dto = ticketService.addCommentToTicket(ticketId, commentInputDto);
        return new ResponseEntity<>(dto, HttpStatus.CREATED); // Status code: 201 Created
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<java.util.List<nl.novi.tickettracker.dtos.CommentOutputDto>> getComments(
            @PathVariable("id") Integer ticketId) {

        java.util.List<nl.novi.tickettracker.dtos.CommentOutputDto> dtos = ticketService.getCommentsForTicket(ticketId);
        return ResponseEntity.ok(dtos); // Status code: 200 Ok
    }
}