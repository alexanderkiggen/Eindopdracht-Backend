package nl.novi.tickettracker.controllers;

import nl.novi.tickettracker.dtos.ProjectInputDto;
import nl.novi.tickettracker.dtos.ProjectOutputDto;
import nl.novi.tickettracker.services.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ProjectControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    public void testCreateProject() throws Exception {

        // Arrange
        String jsonBody = """
                {
                    "name": "Nieuw Project",
                    "description": "Beschrijving etc..."
                }
                """;

        ProjectOutputDto outputDto = new ProjectOutputDto();
        outputDto.setId(1);
        outputDto.setName("Nieuw Project");

        when(projectService.createProject(any(ProjectInputDto.class))).thenReturn(outputDto);

        // Act
        var result = mockMvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Nieuw Project"));
    }
}