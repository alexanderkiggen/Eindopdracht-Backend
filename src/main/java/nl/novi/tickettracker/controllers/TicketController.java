package nl.novi.tickettracker.controllers;

import java.util.Map;
import java.time.Instant;
import java.util.LinkedHashMap;
import jakarta.validation.Valid;
import nl.novi.tickettracker.dtos.*;
import nl.novi.tickettracker.models.FileAttachment;
import nl.novi.tickettracker.services.TicketService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    // Postman Request: [Ticket] Create New Ticket
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketOutputDto> createTicket(
            @ModelAttribute @Valid TicketInputDto ticketInputDto,
            @RequestParam("file") MultipartFile file) throws IOException {

        TicketOutputDto ticketOutputDto = ticketService.createTicket(ticketInputDto, file);
        return new ResponseEntity<>(ticketOutputDto, HttpStatus.CREATED); // Status code: 201 Created
    }

    // Postman Request: [Ticket] Get All Tickets
    @GetMapping
    public ResponseEntity<List<TicketOutputDto>> getAllTickets(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sort", defaultValue = "id") String sortField,
            @RequestParam(value = "dir", defaultValue = "DESC") String sortDirection
    ) {
        return ResponseEntity.ok(ticketService.getAllTickets(page, size, sortField, sortDirection)); // Status code: 200 Ok
    }

    // Postman Request: [Ticket] Get Ticket By Id
    @GetMapping("/{id}")
    public ResponseEntity<TicketOutputDto> getTicketById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(ticketService.getTicketById(id)); // Status code: 200 Ok
    }

    // Postman Request: [Ticket] Assign Developer to Ticket
    @PutMapping("/{id}/assign")
    public ResponseEntity<TicketOutputDto> assignDeveloperToTicket(
            @PathVariable("id") Integer id,
            @Valid @RequestBody AssignDeveloperDto assignDeveloperDto) {

        TicketOutputDto ticketOutputDto = ticketService.assignDeveloperToTicket(id, assignDeveloperDto.getUsername());
        return new ResponseEntity<>(ticketOutputDto, HttpStatus.OK); // Status code: 200 Ok
    }

    // Postman Request: [Ticket] Update Ticket Status
    @PutMapping("/{id}/status")
    public ResponseEntity<TicketOutputDto> updateTicketStatus(
            @PathVariable("id") Integer id,
            @Valid @RequestBody UpdateTicketStatusDto statusDto) {

        TicketOutputDto ticketOutputDto = ticketService.updateTicketStatus(id, statusDto.getStatus());
        return new ResponseEntity<>(ticketOutputDto, HttpStatus.OK); // Status code: 200 Ok
    }

    // Postman Request: [Ticket] Change Project of Ticket
    @PutMapping("/{id}/project")
    public ResponseEntity<TicketOutputDto> changeTicketProject(
            @PathVariable("id") Integer id,
            @Valid @RequestBody UpdateTicketProjectDto projectDto) {

        TicketOutputDto ticketOutputDto = ticketService.changeTicketProject(id, projectDto.getProjectId());
        return new ResponseEntity<>(ticketOutputDto, HttpStatus.OK); // Status code: 200 Ok
    }

    // Postman Request: [Ticket] Delete Ticket
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteTicket(@PathVariable("id") Integer id) {
        ticketService.deleteTicket(id);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Ticket with ID " + id + " was successfully deleted.");

        return ResponseEntity.ok(response); // Status code: 200 Ok
    }

    // Postman Request: [Attachment] Upload File to Ticket
    @PostMapping("/{id}/attachments")
    public ResponseEntity<FileAttachmentOutputDto> uploadFile(
            @PathVariable("id") Integer ticketId,
            @RequestParam("file") MultipartFile file) throws IOException {

        FileAttachmentOutputDto dto = ticketService.uploadFileToTicket(ticketId, file);
        return new ResponseEntity<>(dto, HttpStatus.CREATED); // Status code: 201 Created
    }

    // Postman Request: [Attachment] Download File
    @GetMapping("/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable("attachmentId") Integer attachmentId) {
        FileAttachment fileAttachment = ticketService.downloadFile(attachmentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileAttachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(fileAttachment.getContentType()))
                .body(fileAttachment.getData()); // Status code: 200 Ok
    }

    // Postman Request: [Attachment] Delete File
    @DeleteMapping("/attachments/{attachmentId}")
    public ResponseEntity<Map<String, Object>> deleteAttachment(@PathVariable("attachmentId") Integer attachmentId) {
        ticketService.deleteAttachment(attachmentId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Attachment with ID " + attachmentId + " was successfully deleted.");

        return ResponseEntity.ok(response); // Status code: 200 Ok
    }

    // Postman Request: [Comment] Add Comment
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentOutputDto> addComment(
            @PathVariable("id") Integer ticketId,
            @Valid @RequestBody CommentInputDto commentInputDto) {

        CommentOutputDto dto = ticketService.addCommentToTicket(ticketId, commentInputDto);
        return new ResponseEntity<>(dto, HttpStatus.CREATED); // Status code: 201 Created
    }

    // Postman Request: [Comment] Get Comments
    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentOutputDto>> getComments(@PathVariable("id") Integer ticketId) {
        List<CommentOutputDto> dtos = ticketService.getCommentsForTicket(ticketId);
        return ResponseEntity.ok(dtos); // Status code: 200 Ok
    }

    // Postman Request: [Comment] Delete Comment
    @DeleteMapping("/{ticketId}/comments/{commentId}")
    public ResponseEntity<Map<String, Object>> deleteComment(
            @PathVariable("ticketId") Integer ticketId,
            @PathVariable("commentId") Integer commentId) {
        ticketService.deleteComment(ticketId, commentId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", HttpStatus.OK.value());
        response.put("message", "Comment with ID " + commentId + " was successfully deleted from ticket " + ticketId + ".");

        return ResponseEntity.ok(response); // Status code: 200 Ok
    }

    // Postman Request: [Ticket] Get Tickets By Project
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TicketOutputDto>> getTicketsByProject(@PathVariable("projectId") Integer projectId) {
        return ResponseEntity.ok(ticketService.getTicketsByProjectId(projectId)); // Status code: 200 Ok
    }
}