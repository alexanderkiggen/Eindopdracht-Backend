package nl.novi.tickettracker.services;

import nl.novi.tickettracker.dtos.TicketInputDto;
import nl.novi.tickettracker.dtos.TicketOutputDto;
import nl.novi.tickettracker.exceptions.RecordNotFoundException;
import nl.novi.tickettracker.models.Project;
import nl.novi.tickettracker.models.Ticket;
import nl.novi.tickettracker.repositories.ProjectRepository;
import nl.novi.tickettracker.repositories.TicketRepository;
import org.springframework.stereotype.Service;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;

    public TicketService(TicketRepository ticketRepository, ProjectRepository projectRepository) {
        this.ticketRepository = ticketRepository;
        this.projectRepository = projectRepository;
    }

    public TicketOutputDto createTicket(TicketInputDto ticketInputDto) {
        Ticket ticketEntity = transferToTicket(ticketInputDto);
        Ticket savedTicketEntity = ticketRepository.save(ticketEntity);
        return transferToDto(savedTicketEntity);
    }

    private Ticket transferToTicket(TicketInputDto dto) {
        Ticket ticket = new Ticket();
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setType(dto.getType()); // TODO: Enum gebruiken uit TicketType
        // TODO: Status toevoegen

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RecordNotFoundException("Project with ID " + dto.getProjectId() + " not found."));
        ticket.setProject(project);

        return ticket;
    }

    private TicketOutputDto transferToDto(Ticket ticket) {
        TicketOutputDto dto = new TicketOutputDto();
        dto.setId(ticket.getId());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setStatus(ticket.getStatus());
        dto.setType(ticket.getType());
        dto.setProjectId(ticket.getProject().getId());
        return dto;
    }
}