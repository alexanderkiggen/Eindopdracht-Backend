package nl.novi.tickettracker.services;

import nl.novi.tickettracker.dtos.TicketInputDto;
import nl.novi.tickettracker.dtos.TicketOutputDto;
import nl.novi.tickettracker.exceptions.RecordNotFoundException;
import nl.novi.tickettracker.models.Project;
import nl.novi.tickettracker.models.Ticket;
import nl.novi.tickettracker.models.User;
import nl.novi.tickettracker.repositories.ProjectRepository;
import nl.novi.tickettracker.repositories.TicketRepository;
import nl.novi.tickettracker.repositories.UserRepository;
import nl.novi.tickettracker.dtos.FileAttachmentOutputDto;
import nl.novi.tickettracker.models.FileAttachment;
import nl.novi.tickettracker.repositories.FileAttachmentRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Objects;

import org.springframework.stereotype.Service;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    public TicketService(TicketRepository ticketRepository, ProjectRepository projectRepository, UserRepository userRepository, FileAttachmentRepository fileAttachmentRepository) {
        this.ticketRepository = ticketRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
    }

    // Nieuw Ticket aanmaken
    public TicketOutputDto createTicket(TicketInputDto ticketInputDto) {
        Ticket ticketEntity = transferToTicket(ticketInputDto);
        Ticket savedTicketEntity = ticketRepository.save(ticketEntity);
        return transferToDto(savedTicketEntity);
    }

    // Developer toewijzen aan ticket
    public TicketOutputDto assignDeveloperToTicket(Integer ticketId, String username) {

        Ticket ticketEntity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with   ID " + ticketId + " not found."));

        User userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RecordNotFoundException("User with username '" + username + "' not found."));

        ticketEntity.setAssignedUser(userEntity);
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

        if (ticket.getAssignedUser() != null) {
            dto.setAssignedUsername(ticket.getAssignedUser().getUsername());
        }

        return dto;
    }

    public FileAttachmentOutputDto uploadFileToTicket(Integer ticketId, MultipartFile file) throws IOException {
        Ticket ticketEntity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with ID " + ticketId + " not found."));

        FileAttachment attachment = new FileAttachment();
        attachment.setFileName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
        attachment.setContentType(file.getContentType());
        attachment.setData(file.getBytes());
        attachment.setTicket(ticketEntity);

        FileAttachment savedAttachment = fileAttachmentRepository.save(attachment);

        FileAttachmentOutputDto dto = new FileAttachmentOutputDto();
        dto.setId(savedAttachment.getId());
        dto.setFileName(savedAttachment.getFileName());
        dto.setContentType(savedAttachment.getContentType());
        return dto;
    }

    public FileAttachment downloadFile(Integer attachmentId) {
        return fileAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RecordNotFoundException("File with ID " + attachmentId + " not found."));
    }
}