package nl.novi.tickettracker.services;

import nl.novi.tickettracker.dtos.FileAttachmentOutputDto;
import nl.novi.tickettracker.dtos.TicketInputDto;
import nl.novi.tickettracker.dtos.TicketOutputDto;
import nl.novi.tickettracker.dtos.CommentInputDto;
import nl.novi.tickettracker.dtos.CommentOutputDto;
import nl.novi.tickettracker.exceptions.RecordNotFoundException;
import nl.novi.tickettracker.models.FileAttachment;
import nl.novi.tickettracker.models.Project;
import nl.novi.tickettracker.models.Ticket;
import nl.novi.tickettracker.models.User;
import nl.novi.tickettracker.models.Comment;
import nl.novi.tickettracker.models.TicketStatus;
import nl.novi.tickettracker.repositories.FileAttachmentRepository;
import nl.novi.tickettracker.repositories.ProjectRepository;
import nl.novi.tickettracker.repositories.TicketRepository;
import nl.novi.tickettracker.repositories.UserRepository;
import nl.novi.tickettracker.repositories.CommentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final CommentRepository commentRepository;

    public TicketService(TicketRepository ticketRepository, ProjectRepository projectRepository, UserRepository userRepository, FileAttachmentRepository fileAttachmentRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.commentRepository = commentRepository;
    }

    // Nieuw Ticket aanmaken
    public TicketOutputDto createTicket(TicketInputDto ticketInputDto, MultipartFile file) throws IOException {
        // Stap 1. Maak het ticket
        Ticket ticketEntity = transferToTicket(ticketInputDto);
        Ticket savedTicketEntity = ticketRepository.save(ticketEntity);

        // Stap 2. Koppel het verplichte bestand
        FileAttachment attachment = new FileAttachment();
        attachment.setFileName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
        attachment.setContentType(file.getContentType());
        attachment.setData(file.getBytes());
        attachment.setTicket(savedTicketEntity);
        fileAttachmentRepository.save(attachment);

        return transferToDto(savedTicketEntity);
    }

    // Developer toewijzen aan ticket
    public TicketOutputDto assignDeveloperToTicket(Integer ticketId, String username) {
        Ticket ticketEntity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with ID " + ticketId + " not found."));

        User userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RecordNotFoundException("User with username '" + username + "' not found."));

        ticketEntity.setAssignedUser(userEntity);
        Ticket savedTicketEntity = ticketRepository.save(ticketEntity);

        return transferToDto(savedTicketEntity);
    }

    // Status van een ticket updaten
    public TicketOutputDto updateTicketStatus(Integer ticketId, TicketStatus newStatus) {
        Ticket ticketEntity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with ID " + ticketId + " not found."));

        ticketEntity.setStatus(newStatus);
        Ticket savedTicketEntity = ticketRepository.save(ticketEntity);

        return transferToDto(savedTicketEntity);
    }

    private Ticket transferToTicket(TicketInputDto dto) {
        Ticket ticket = new Ticket();
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setType(dto.getType());

        // Stap 1. Koppel het verplichte Project
        ticket.setStatus(dto.getStatus());

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RecordNotFoundException("Project with ID " + dto.getProjectId() + " not found."));
        ticket.setProject(project);

        // Stap 2. Koppel de verplichte User
        User user = userRepository.findByUsername(dto.getAssignedUsername())
                .orElseThrow(() -> new RecordNotFoundException("User with username '" + dto.getAssignedUsername() + "' not found."));
        ticket.setAssignedUser(user);

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

        if (ticket.getId() != null) {
            // Stap 1. Files ophalen
            List<FileAttachment> attachments = fileAttachmentRepository.findByTicketId(ticket.getId());
            List<FileAttachmentOutputDto> fileDtos = attachments.stream().map(att -> {
                FileAttachmentOutputDto fDto = new FileAttachmentOutputDto();
                fDto.setId(att.getId());
                fDto.setFileName(att.getFileName());
                fDto.setContentType(att.getContentType());
                return fDto;
            }).toList();
            dto.setFiles(fileDtos);

            // Stap 2. Comments ophalen
            List<Comment> comments = commentRepository.findByTicketId(ticket.getId());
            List<CommentOutputDto> commentDtos = comments.stream().map(c -> {
                CommentOutputDto cDto = new CommentOutputDto();
                cDto.setId(c.getId());
                cDto.setText(c.getText());
                cDto.setTimestamp(c.getTimestamp());
                return cDto;
            }).toList();
            dto.setComments(commentDtos);
        }

        return dto;
    }

    // Extra bestand toevoegen aan een bestaand ticket
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

    // Comments
    public CommentOutputDto addCommentToTicket(Integer ticketId, CommentInputDto dto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with ID " + ticketId + " not found."));

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setTimestamp(java.time.LocalDateTime.now());
        comment.setTicket(ticket);

        Comment savedComment = commentRepository.save(comment);

        CommentOutputDto output = new CommentOutputDto();
        output.setId(savedComment.getId());
        output.setText(savedComment.getText());
        output.setTimestamp(savedComment.getTimestamp());

        return output;
    }

    public List<CommentOutputDto> getCommentsForTicket(Integer ticketId) {
        List<Comment> comments = commentRepository.findByTicketId(ticketId);

        return comments.stream().map(c -> {
            CommentOutputDto dto = new CommentOutputDto();
            dto.setId(c.getId());
            dto.setText(c.getText());
            dto.setTimestamp(c.getTimestamp());
            return dto;
        }).toList();
    }

    // Tickets ophalen
    public List<TicketOutputDto> getAllTickets() {
        List<Ticket> tickets = ticketRepository.findAll();
        return tickets.stream().map(this::transferToDto).toList();
    }

    public TicketOutputDto getTicketById(Integer id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with ID " + id + " not found."));
        return transferToDto(ticket);
    }

    public List<TicketOutputDto> getTicketsByProjectId(Integer projectId) {
        List<Ticket> tickets = ticketRepository.findByProjectId(projectId);
        return tickets.stream().map(this::transferToDto).toList();
    }
}