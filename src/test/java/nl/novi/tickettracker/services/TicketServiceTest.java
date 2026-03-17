package nl.novi.tickettracker.services;

import nl.novi.tickettracker.dtos.*;
import nl.novi.tickettracker.exceptions.RecordNotFoundException;
import nl.novi.tickettracker.models.*;
import nl.novi.tickettracker.repositories.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileAttachmentRepository fileAttachmentRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    public void testGetTicketById_Success() {
        // Arrange
        Project project = new Project();
        project.setId(1);

        User user = new User();
        user.setUsername("johndoe");

        Ticket ticket = new Ticket();
        ticket.setId(100);
        ticket.setTitle("Test Ticket");
        ticket.setDescription("Dit is een test etc...");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setType(TicketType.BUG);
        ticket.setProject(project);
        ticket.setAssignedUser(user);

        FileAttachment file = new FileAttachment();
        file.setId(1);
        file.setFileName("test.pdf");
        file.setContentType("application/pdf");
        List<FileAttachment> files = List.of(file);

        Comment comment = new Comment();
        comment.setId(1);
        comment.setText("Test comment etc...");
        comment.setTimestamp(LocalDateTime.now());
        List<Comment> comments = List.of(comment);

        when(ticketRepository.findById(100)).thenReturn(Optional.of(ticket));
        when(fileAttachmentRepository.findByTicketId(100)).thenReturn(files);
        when(commentRepository.findByTicketId(100)).thenReturn(comments);

        // Act
        TicketOutputDto result = ticketService.getTicketById(100);

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getId());
        assertEquals("johndoe", result.getAssignedUsername());
        assertEquals(1, result.getFiles().size());
        assertEquals(1, result.getComments().size());
    }

    @Test
    public void testGetTicketById_ThrowsRecordNotFoundException() {
        // Arrange
        when(ticketRepository.findById(999)).thenReturn(Optional.empty());

        // Act
        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> ticketService.getTicketById(999));

        // Assert
        assertEquals("Ticket with ID 999 not found.", exception.getMessage());
    }

    @Test
    public void testCreateTicket_Success() throws IOException {
        // Arrange
        TicketInputDto dto = new TicketInputDto();
        dto.setTitle("Nieuwe Bug");
        dto.setDescription("Bug beschrijving etc...");
        dto.setType(TicketType.BUG);
        dto.setStatus(TicketStatus.OPEN);
        dto.setProjectId(1);
        dto.setAssignedUsername("johndoe");

        Project project = new Project();
        project.setId(1);

        User user = new User();
        user.setUsername("johndoe");

        Ticket savedTicket = new Ticket();
        savedTicket.setId(50);
        savedTicket.setTitle("Nieuwe Bug");
        savedTicket.setProject(project);
        savedTicket.setAssignedUser(user);

        MultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Hello World".getBytes());

        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedTicket);

        // Act
        TicketOutputDto result = ticketService.createTicket(dto, mockFile);

        // Assert
        assertNotNull(result);
        assertEquals(50, result.getId());
    }

    @Test
    public void testCreateTicket_ProjectNotFound() {
        // Arrange
        TicketInputDto dto = new TicketInputDto();
        dto.setProjectId(99);
        MultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test".getBytes());

        when(projectRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> ticketService.createTicket(dto, mockFile));
    }

    @Test
    public void testCreateTicket_UserNotFound() {
        // Arrange
        TicketInputDto dto = new TicketInputDto();
        dto.setProjectId(1);
        dto.setAssignedUsername("onbekend");
        MultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Test".getBytes());

        Project project = new Project();
        project.setId(1);

        when(projectRepository.findById(1)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("onbekend")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> ticketService.createTicket(dto, mockFile));
    }

    @Test
    public void testAssignDeveloperToTicket_Success() {
        // Arrange
        Ticket ticket = new Ticket();
        ticket.setId(10);
        Project project = new Project();
        project.setId(1);
        ticket.setProject(project);

        User user = new User();
        user.setUsername("johndoe");

        when(ticketRepository.findById(10)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // Act
        TicketOutputDto result = ticketService.assignDeveloperToTicket(10, "johndoe");

        // Assert
        assertEquals("johndoe", result.getAssignedUsername());
    }

    @Test
    public void testAssignDeveloperToTicket_TicketNotFound() {
        // Arrange
        when(ticketRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> ticketService.assignDeveloperToTicket(99, "onbekend"));
    }

    @Test
    public void testAssignDeveloperToTicket_UserNotFound() {
        // Arrange
        Ticket ticket = new Ticket();
        when(ticketRepository.findById(10)).thenReturn(Optional.of(ticket));
        when(userRepository.findByUsername("dev1")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> ticketService.assignDeveloperToTicket(10, "dev1"));
    }

    @Test
    public void testUpdateTicketStatus_Success() {
        // Arrange
        Ticket ticket = new Ticket();
        ticket.setId(1);
        Project project = new Project();
        project.setId(1);
        ticket.setProject(project);

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        // Act
        TicketOutputDto result = ticketService.updateTicketStatus(1, TicketStatus.IN_PROGRESS);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testUpdateTicketStatus_NotFound() {
        // Arrange
        when(ticketRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> ticketService.updateTicketStatus(1, TicketStatus.CLOSED));
    }

    @Test
    public void testGetAllTickets_PagedAndSorted_Descending() {
        // Arrange
        Project project = new Project();
        project.setId(1);
        Ticket ticket1 = new Ticket();
        ticket1.setId(1);
        ticket1.setProject(project);

        Page<Ticket> pagedResponse = new PageImpl<>(List.of(ticket1));
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"));

        when(ticketRepository.findAll(expectedPageable)).thenReturn(pagedResponse);

        // Act
        List<TicketOutputDto> result = ticketService.getAllTickets(0, 10, "id", "DESC");

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testGetAllTickets_PagedAndSorted_Ascending() {
        // Arrange
        Project project = new Project();
        project.setId(1);
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setProject(project);

        Page<Ticket> pagedResponse = new PageImpl<>(List.of(ticket));

        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "id"));
        when(ticketRepository.findAll(expectedPageable)).thenReturn(pagedResponse);

        // Act
        List<TicketOutputDto> result = ticketService.getAllTickets(0, 10, "id", "ASC");

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testGetTicketsByProjectId_Success() {
        // Arrange
        Project project = new Project();
        project.setId(1);
        Ticket ticket = new Ticket();
        ticket.setId(5);
        ticket.setProject(project);

        when(projectRepository.existsById(1)).thenReturn(true);
        when(ticketRepository.findByProjectId(1)).thenReturn(List.of(ticket));

        // Act
        List<TicketOutputDto> result = ticketService.getTicketsByProjectId(1);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testGetTicketsByProjectId_NotFound() {
        // Arrange
        when(projectRepository.existsById(1)).thenReturn(false);

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> ticketService.getTicketsByProjectId(1));
    }

    @Test
    public void testTransferToDto_TicketWithoutId() {
        // Arrange
        Project project = new Project();
        project.setId(1);

        Ticket ticket = new Ticket();
        ticket.setTitle("Ticket zonder ID");
        ticket.setProject(project);

        when(projectRepository.existsById(1)).thenReturn(true);
        when(ticketRepository.findByProjectId(1)).thenReturn(List.of(ticket));

        // Act
        List<TicketOutputDto> result = ticketService.getTicketsByProjectId(1);

        // Assert
        assertEquals(1, result.size());
        assertNull(result.getFirst().getId());
    }

    @Test
    public void testUploadFileToTicket_Success() throws IOException {
        // Arrange
        Ticket ticket = new Ticket();
        ticket.setId(1);

        FileAttachment attachment = new FileAttachment();
        attachment.setId(100);
        attachment.setFileName("test.txt");
        attachment.setContentType("text/plain");

        MultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Data".getBytes());

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(fileAttachmentRepository.save(any(FileAttachment.class))).thenReturn(attachment);

        // Act
        FileAttachmentOutputDto result = ticketService.uploadFileToTicket(1, mockFile);

        // Assert
        assertEquals(100, result.getId());
        assertEquals("test.txt", result.getFileName());
    }

    @Test
    public void testUploadFileToTicket_NotFound() {
        // Arrange
        MultipartFile mockFile = new MockMultipartFile("file", "test.txt", "text/plain", "Data".getBytes());
        when(ticketRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> ticketService.uploadFileToTicket(1, mockFile));
    }

    @Test
    public void testDownloadFile_Success() {
        // Arrange
        FileAttachment attachment = new FileAttachment();
        attachment.setId(1);
        when(fileAttachmentRepository.findById(1)).thenReturn(Optional.of(attachment));

        // Act
        FileAttachment result = ticketService.downloadFile(1);

        // Assert
        assertEquals(1, result.getId());
    }

    @Test
    public void testDownloadFile_NotFound() {
        // Arrange
        when(fileAttachmentRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> ticketService.downloadFile(1));
    }

    @Test
    public void testAddCommentToTicket_Success() {
        // Arrange
        Ticket ticket = new Ticket();
        ticket.setId(1);

        CommentInputDto dto = new CommentInputDto();
        dto.setText("Opmerking etc...");

        Comment savedComment = new Comment();
        savedComment.setId(5);
        savedComment.setText("Opmerking etc...");
        savedComment.setTimestamp(LocalDateTime.now());

        when(ticketRepository.findById(1)).thenReturn(Optional.of(ticket));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // Act
        CommentOutputDto result = ticketService.addCommentToTicket(1, dto);

        // Assert
        assertEquals(5, result.getId());
        assertEquals("Opmerking etc...", result.getText());
    }

    @Test
    public void testAddCommentToTicket_NotFound() {
        // Arrange
        CommentInputDto dto = new CommentInputDto();
        when(ticketRepository.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> ticketService.addCommentToTicket(1, dto));
    }

    @Test
    public void testGetCommentsForTicket_Success() {
        // Arrange
        Comment comment = new Comment();
        comment.setId(1);
        comment.setText("Test");
        comment.setTimestamp(LocalDateTime.now());

        when(ticketRepository.existsById(1)).thenReturn(true);
        when(commentRepository.findByTicketId(1)).thenReturn(List.of(comment));

        // Act
        List<CommentOutputDto> result = ticketService.getCommentsForTicket(1);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    public void testGetCommentsForTicket_NotFound() {
        // Arrange
        when(ticketRepository.existsById(1)).thenReturn(false);

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> ticketService.getCommentsForTicket(1));
    }
}