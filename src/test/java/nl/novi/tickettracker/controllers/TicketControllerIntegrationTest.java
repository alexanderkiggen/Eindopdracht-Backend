package nl.novi.tickettracker.controllers;

import nl.novi.tickettracker.models.*;
import nl.novi.tickettracker.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class TicketControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private FileAttachmentRepository fileAttachmentRepository;
    @Autowired private CommentRepository commentRepository;

    private Integer testProjectId;
    private Integer testTicketId;
    private Integer testAttachmentId;
    private final String testUsername = "johndoe";

    @BeforeEach
    void setUp() {
        // Keycloack mock
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("preferred_username", testUsername)
                .build();
        org.springframework.security.core.Authentication auth =
                new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_DEVELOPER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Data voor de tests
        Project project = new Project();
        project.setName("Test Project");
        project = projectRepository.save(project);
        testProjectId = project.getId();

        UserProfile profile = new UserProfile();
        profile.setEmail("johndoe@novi-education.nl");
        User user = new User();
        user.setUsername(testUsername);
        user.setUserProfile(profile);
        userRepository.save(user);

        UserProfile profile2 = new UserProfile();
        profile2.setEmail("janedoe@novi-education.nl");
        profile2.setFirstname("Jane");
        profile2.setLastname("Doe");
        User dev = new User();
        dev.setUsername("janedoe");
        dev.setUserProfile(profile2);
        userRepository.save(dev);

        Ticket ticket = new Ticket();
        ticket.setTitle("Test Ticket");
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setType(TicketType.BUG);
        ticket.setProject(project);
        ticket.setAssignedUser(user);
        ticket = ticketRepository.save(ticket);
        testTicketId = ticket.getId();

        FileAttachment file = new FileAttachment();
        file.setFileName("download.pdf");
        file.setContentType("application/pdf");
        file.setData("Hello World".getBytes());
        file.setTicket(ticket);
        file = fileAttachmentRepository.save(file);
        testAttachmentId = file.getId();

        Comment comment = new Comment();
        comment.setText("Bestaande opmerking");
        comment.setTimestamp(LocalDateTime.now());
        comment.setTicket(ticket);
        commentRepository.save(comment);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreateTicket() throws Exception {

        // Arrange
        MockMultipartFile filePart = new MockMultipartFile("file", "test.png", "image/png", "data".getBytes());

        // Act
        var result = mockMvc.perform(multipart("/tickets")
                .file(filePart)
                .param("title", "Nieuwe Bug")
                .param("description", "Beschrijving etc...")
                .param("status", "OPEN")
                .param("type", "BUG")
                .param("projectId", String.valueOf(testProjectId))
                .param("assignedUsername", testUsername));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Nieuwe Bug"));
    }

    @Test
    public void testAssignDeveloperToTicket() throws Exception {

        // Arrange
        String json = """
                {
                    "username": "janedoe"
                }
                """;

        // Act
        var result = mockMvc.perform(put("/tickets/" + testTicketId + "/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.assignedUsername").value("janedoe"));
    }

    @Test
    public void testUploadFile() throws Exception {

        // Arrange
        MockMultipartFile filePart = new MockMultipartFile("file", "doc.pdf", "application/pdf", "data".getBytes());

        // Act
        var result = mockMvc.perform(multipart("/tickets/" + testTicketId + "/attachments").file(filePart));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("doc.pdf"));
    }

    @Test
    public void testDownloadFile() throws Exception {

        // Act
        var result = mockMvc.perform(get("/tickets/attachments/" + testAttachmentId));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"download.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().bytes("Hello World".getBytes()));
    }

    @Test
    public void testAddComment() throws Exception {

        // Arrange
        String json = """
                {
                    "text": "Dit is een integratietest comment!"
                }
                """;

        // Act
        var result = mockMvc.perform(post("/tickets/" + testTicketId + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("Dit is een integratietest comment!"));
    }

    @Test
    public void testGetComments() throws Exception {

        // Act
        var result = mockMvc.perform(get("/tickets/" + testTicketId + "/comments"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("Bestaande opmerking"));
    }

    @Test
    public void testGetAllTickets() throws Exception {

        // Act
        var result = mockMvc.perform(get("/tickets?page=0&size=10&sort=id&dir=DESC"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    public void testGetTicketById() throws Exception {

        // Act
        var result = mockMvc.perform(get("/tickets/" + testTicketId));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTicketId));
    }

    @Test
    public void testGetTicketsByProject() throws Exception {

        // Act
        var result = mockMvc.perform(get("/tickets/project/" + testProjectId));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].projectId").value(testProjectId));
    }

    @Test
    public void testUpdateTicketStatus() throws Exception {

        // Arrange
        String json = """
                {
                    "status": "CLOSED"
                }
                """;

        // Act
        var result = mockMvc.perform(put("/tickets/" + testTicketId + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    public void testChangeTicketProject() throws Exception {

        // Arrange
        String json = """
                {
                    "projectId": %d
                }
                """.formatted(testProjectId);

        // Act
        var result = mockMvc.perform(put("/tickets/" + testTicketId + "/project")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(testProjectId));
    }
}