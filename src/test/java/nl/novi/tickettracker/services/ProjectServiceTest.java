package nl.novi.tickettracker.services;

import nl.novi.tickettracker.dtos.ProjectInputDto;
import nl.novi.tickettracker.dtos.ProjectOutputDto;
import nl.novi.tickettracker.models.Project;
import nl.novi.tickettracker.repositories.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    public void testCreateProject_WithoutStartDate() {
        // Arrange
        ProjectInputDto inputDto = new ProjectInputDto();
        inputDto.setName("Project naam");
        inputDto.setDescription("Projectbeschrijving etc...");
        inputDto.setStartDate(null);

        Project savedProject = new Project();
        savedProject.setId(1);
        savedProject.setName("Nieuwe project naam");
        savedProject.setDescription("Projectbeschrijving etc...");
        savedProject.setStartDate(LocalDateTime.now());

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        // Act
        ProjectOutputDto result = projectService.createProject(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Nieuwe project naam", result.getName());
        assertNotNull(result.getStartDate());

        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    public void testCreateProject_WithProvidedStartDate() {
        // Arrange
        LocalDateTime customDate = LocalDateTime.of(2026, 5, 1, 10, 0);

        ProjectInputDto inputDto = new ProjectInputDto();
        inputDto.setName("Toekomstig project");
        inputDto.setDescription("Projectbeschrijving etc...");
        inputDto.setStartDate(customDate);

        Project savedProject = new Project();
        savedProject.setId(2);
        savedProject.setName("Nieuw toekomstig project");
        savedProject.setDescription("Projectbeschrijving etc...");
        savedProject.setStartDate(customDate);

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        // Act
        ProjectOutputDto result = projectService.createProject(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getId());
        assertEquals("Nieuw toekomstig project", result.getName());
        assertEquals(customDate, result.getStartDate());

        verify(projectRepository, times(1)).save(any(Project.class));
    }
}