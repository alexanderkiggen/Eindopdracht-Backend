package nl.novi.tickettracker.controllers;

import jakarta.validation.Valid;
import nl.novi.tickettracker.dtos.ProjectInputDto;
import nl.novi.tickettracker.dtos.ProjectOutputDto;
import nl.novi.tickettracker.services.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectOutputDto> createProject(@Valid @RequestBody ProjectInputDto projectInputDto) {
        ProjectOutputDto dto = projectService.createProject(projectInputDto);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
}