package nl.novi.tickettracker.controllers;

import nl.novi.tickettracker.models.Project;
import nl.novi.tickettracker.repositories.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    private Integer testProjectId;

    @BeforeEach
    void setUp() {
        Project project = new Project();
        project.setName("Bestaand Project");
        project.setDescription("Bestaande beschrijving...");
        project = projectRepository.save(project);
        testProjectId = project.getId();
    }

    @Test
    public void testCreateProject_Success() throws Exception {

        // Arrange
        String jsonBody = """
                {
                    "name": "Nieuw Project",
                    "description": "Beschrijving etc..."
                }
                """;

        // Act
        var result = mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Nieuw Project"))
                .andExpect(jsonPath("$.description").value("Beschrijving etc..."));
    }

    @Test
    public void testGetAllProjects_Success() throws Exception {

        // Act
        var result = mockMvc.perform(get("/projects"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    public void testGetProjectById_Success() throws Exception {
        // Act
        var result = mockMvc.perform(get("/projects/" + testProjectId));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bestaand Project"));
    }

    @Test
    public void testUpdateProject_Success() throws Exception {

        // Arrange
        String jsonBody = """
                {
                    "name": "Geupdatet Project",
                    "description": "Nieuwe beschrijving..."
                }
                """;

        // Act
        var result = mockMvc.perform(put("/projects/" + testProjectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Geupdatet Project"))
                .andExpect(jsonPath("$.description").value("Nieuwe beschrijving..."));
    }
}