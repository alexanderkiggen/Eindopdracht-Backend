package nl.novi.tickettracker.services;

import nl.novi.tickettracker.dtos.ProjectInputDto;
import nl.novi.tickettracker.dtos.ProjectOutputDto;
import nl.novi.tickettracker.models.Project;
import nl.novi.tickettracker.repositories.ProjectRepository;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectOutputDto createProject(ProjectInputDto dto) {
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());

        // Als startdatum niet is meegegeven dan huidige tijd gebruiken
        if (dto.getStartDate() == null) {
            project.setStartDate(java.time.LocalDateTime.now());
        } else {
            project.setStartDate(dto.getStartDate());
        }

        Project savedProject = projectRepository.save(project);

        return transferToDto(savedProject);
    }

    private ProjectOutputDto transferToDto(Project project) {
        ProjectOutputDto dto = new ProjectOutputDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setStartDate(project.getStartDate());
        return dto;
    }
}