package nl.novi.tickettracker.controllers;

import nl.novi.tickettracker.dtos.*;
import nl.novi.tickettracker.models.FileAttachment;
import nl.novi.tickettracker.models.TicketStatus;
import nl.novi.tickettracker.services.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    public void testCreateTicket() throws Exception {
        // Arrange
        String ticketJson = """
                {
                    "title": "Bug",
                    "description": "Beschrijving etc...",
                    "status": "OPEN",
                    "type": "BUG",
                    "projectId": 1,
                    "assignedUsername": "jonhdoe"
                }
                """;

        MockMultipartFile ticketPart = new MockMultipartFile("ticket", "", "application/json", ticketJson.getBytes());
        MockMultipartFile filePart = new MockMultipartFile("file", "test.png", "image/png", "data".getBytes());

        TicketOutputDto output = new TicketOutputDto();
        output.setId(1);
        output.setTitle("Bug");

        when(ticketService.createTicket(any(), any())).thenReturn(output);

        // Act & Assert
        mockMvc.perform(multipart("/tickets")
                        .file(ticketPart)
                        .file(filePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testAssignDeveloperToTicket() throws Exception {
        // Arrange
        String json = """
                {
                    "username": "johndoe"
                }
                """;

        TicketOutputDto output = new TicketOutputDto();
        output.setAssignedUsername("johndoe");

        when(ticketService.assignDeveloperToTicket(eq(1), eq("johndoe"))).thenReturn(output);

        // Act & Assert
        mockMvc.perform(put("/tickets/1/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUsername").value("johndoe"));
    }

    @Test
    public void testUploadFile() throws Exception {
        // Arrange
        MockMultipartFile filePart = new MockMultipartFile("file", "doc.pdf", "application/pdf", "data".getBytes());
        FileAttachmentOutputDto output = new FileAttachmentOutputDto();
        output.setFileName("doc.pdf");

        when(ticketService.uploadFileToTicket(eq(1), any())).thenReturn(output);

        // Act & Assert
        mockMvc.perform(multipart("/tickets/1/attachments").file(filePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("doc.pdf"));
    }

    @Test
    public void testDownloadFile() throws Exception {
        // Arrange
        FileAttachment attachment = new FileAttachment();
        attachment.setFileName("doc.pdf");
        attachment.setContentType("application/pdf");
        attachment.setData("Hello World".getBytes());

        when(ticketService.downloadFile(1)).thenReturn(attachment);

        // Act & Assert
        mockMvc.perform(get("/tickets/attachments/1"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"doc.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes("Hello World".getBytes()));
    }

    @Test
    public void testAddComment() throws Exception {
        // Arrange
        String json = """
                {
                    "text": "Beschrijving etc..."
                }
                """;
        CommentOutputDto output = new CommentOutputDto();
        output.setText("Beschrijving etc...");

        when(ticketService.addCommentToTicket(eq(1), any(nl.novi.tickettracker.dtos.CommentInputDto.class))).thenReturn(output);

        // Act & Assert
        mockMvc.perform(post("/tickets/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Beschrijving etc..."));
    }

    @Test
    public void testGetComments() throws Exception {
        // Arrange
        CommentOutputDto output = new CommentOutputDto();
        output.setText("Beschrijving etc...");

        when(ticketService.getCommentsForTicket(1)).thenReturn(List.of(output));

        // Act & Assert
        mockMvc.perform(get("/tickets/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Beschrijving etc..."));
    }

    @Test
    public void testGetAllTickets() throws Exception {
        // Arrange
        TicketOutputDto output = new TicketOutputDto();
        output.setId(10);

        when(ticketService.getAllTickets(0, 10, "id", "DESC")).thenReturn(List.of(output));

        // Act & Assert
        mockMvc.perform(get("/tickets?page=0&size=10&sort=id&dir=DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    public void testGetTicketById() throws Exception {
        // Arrange
        TicketOutputDto output = new TicketOutputDto();
        output.setId(5);

        when(ticketService.getTicketById(5)).thenReturn(output);

        // Act & Assert
        mockMvc.perform(get("/tickets/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    public void testGetTicketsByProject() throws Exception {
        // Arrange
        TicketOutputDto output = new TicketOutputDto();
        output.setId(1);

        when(ticketService.getTicketsByProjectId(99)).thenReturn(List.of(output));

        // Act & Assert
        mockMvc.perform(get("/tickets/project/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    public void testUpdateTicketStatus() throws Exception {
        // Arrange
        String json = """
                {
                    "status": "CLOSED"
                }
                """;
        TicketOutputDto output = new TicketOutputDto();
        output.setStatus(TicketStatus.CLOSED);

        when(ticketService.updateTicketStatus(eq(1), eq(TicketStatus.CLOSED))).thenReturn(output);

        // Act & Assert
        mockMvc.perform(put("/tickets/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
}