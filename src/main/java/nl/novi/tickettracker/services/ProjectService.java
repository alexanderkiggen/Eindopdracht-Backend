package nl.novi.tickettracker.services;

import nl.novi.tickettracker.dtos.ProjectInputDto;
import nl.novi.tickettracker.dtos.ProjectOutputDto;
import nl.novi.tickettracker.dtos.ProjectUpdateDto;
import nl.novi.tickettracker.models.Project;
import nl.novi.tickettracker.repositories.ProjectRepository;
import nl.novi.tickettracker.exceptions.RecordNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

        return transferToDto(projectRepository.save(project));
    }

    public List<ProjectOutputDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::transferToDto)
                .collect(Collectors.toList());
    }

    public ProjectOutputDto getProjectById(Integer id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Project with ID " + id + " not found."));
        return transferToDto(project);
    }

    public ProjectOutputDto updateProject(Integer id, ProjectUpdateDto dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Project with ID " + id + " not found."));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            project.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            project.setDescription(dto.getDescription());
        }
        if (dto.getStartDate() != null) {
            project.setStartDate(dto.getStartDate());
        }

        return transferToDto(projectRepository.save(project));
    }

    public ProjectOutputDto updateProject(Integer id, ProjectInputDto dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Project with ID " + id + " not found."));

        if (dto.getName() != null && !dto.getName().isBlank()) {
            project.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            project.setDescription(dto.getDescription());
        }
        if (dto.getStartDate() != null) {
            project.setStartDate(dto.getStartDate());
        }

        return transferToDto(projectRepository.save(project));
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