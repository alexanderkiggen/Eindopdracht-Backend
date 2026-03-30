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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    private void validateDeveloperOwnership(Ticket ticket) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String loggedInUsername = jwt.getClaimAsString("preferred_username");

            boolean isDeveloper = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_DEVELOPER"));
            boolean isProjectManager = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_PROJECTMANAGER"));

            if (isDeveloper && !isProjectManager) {
                if (ticket.getAssignedUser() == null || !ticket.getAssignedUser().getUsername().equals(loggedInUsername)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to modify a ticket that is not assigned to you unless you are a project manager.");
                }
            }
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File cannot be empty.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid file type. Only images (PNG, JPEG, WEBP, GIF) and documents (PDF, DOC, DOCX, TXT) are allowed.");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        List<String> allowedTypes = List.of(
                "image/png",
                "image/jpeg",
                "image/webp",
                "image/gif",
                "application/pdf",
                "application/msword", // .doc
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                "text/plain" // .txt
        );
        return allowedTypes.contains(contentType);
    }

    public TicketOutputDto createTicket(TicketInputDto ticketInputDto, MultipartFile file) throws IOException {
        validateFile(file);

        Ticket ticketEntity = transferToTicket(ticketInputDto);
        Ticket savedTicketEntity = ticketRepository.save(ticketEntity);

        FileAttachment attachment = new FileAttachment();
        attachment.setFileName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
        attachment.setContentType(file.getContentType());
        attachment.setData(file.getBytes());
        attachment.setTicket(savedTicketEntity);
        fileAttachmentRepository.save(attachment);

        return transferToDto(savedTicketEntity);
    }

    public TicketOutputDto assignDeveloperToTicket(Integer ticketId, String username) {
        Ticket ticketEntity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with ID " + ticketId + " not found."));

        User userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new RecordNotFoundException("User with username '" + username + "' not found."));

        ticketEntity.setAssignedUser(userEntity);
        Ticket savedTicketEntity = ticketRepository.save(ticketEntity);

        return transferToDto(savedTicketEntity);
    }

    public TicketOutputDto updateTicketStatus(Integer ticketId, TicketStatus newStatus) {
        Ticket ticketEntity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with ID " + ticketId + " not found."));

        validateDeveloperOwnership(ticketEntity);
        ticketEntity.setStatus(newStatus);
        Ticket savedTicketEntity = ticketRepository.save(ticketEntity);

        return transferToDto(savedTicketEntity);
    }

    public TicketOutputDto changeTicketProject(Integer ticketId, Integer projectId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with ID " + ticketId + " not found."));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RecordNotFoundException("Project with ID " + projectId + " not found."));

        ticket.setProject(project);
        Ticket savedTicket = ticketRepository.save(ticket);

        return transferToDto(savedTicket);
    }

    public FileAttachmentOutputDto uploadFileToTicket(Integer ticketId, MultipartFile file) throws IOException {
        Ticket ticketEntity = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with ID " + ticketId + " not found."));

        validateDeveloperOwnership(ticketEntity);
        validateFile(file);

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


    public CommentOutputDto addCommentToTicket(Integer ticketId, CommentInputDto dto) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with ID " + ticketId + " not found."));

        validateDeveloperOwnership(ticket);

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
        if (!ticketRepository.existsById(ticketId)) {
            throw new RecordNotFoundException("Ticket with ID " + ticketId + " not found.");
        }

        List<Comment> comments = commentRepository.findByTicketId(ticketId);

        return comments.stream().map(c -> {
            CommentOutputDto dto = new CommentOutputDto();
            dto.setId(c.getId());
            dto.setText(c.getText());
            dto.setTimestamp(c.getTimestamp());
            return dto;
        }).toList();
    }

    public List<TicketOutputDto> getAllTickets(int page, int size, String sortField, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<Ticket> ticketPage = ticketRepository.findAll(pageable);

        return ticketPage.stream()
                .map(this::transferToDto)
                .collect(Collectors.toList());
    }

    public TicketOutputDto getTicketById(Integer id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("Ticket with ID " + id + " not found."));
        return transferToDto(ticket);
    }

    public List<TicketOutputDto> getTicketsByProjectId(Integer projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RecordNotFoundException("Project with ID " + projectId + " not found.");
        }

        List<Ticket> tickets = ticketRepository.findByProjectId(projectId);
        return tickets.stream().map(this::transferToDto).toList();
    }

    private Ticket transferToTicket(TicketInputDto dto) {
        Ticket ticket = new Ticket();
        ticket.setTitle(dto.getTitle());
        ticket.setDescription(dto.getDescription());
        ticket.setType(dto.getType());
        ticket.setStatus(dto.getStatus());

        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RecordNotFoundException("Project with ID " + dto.getProjectId() + " not found."));
        ticket.setProject(project);

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
            List<FileAttachment> attachments = fileAttachmentRepository.findByTicketId(ticket.getId());
            List<FileAttachmentOutputDto> fileDtos = attachments.stream().map(att -> {
                FileAttachmentOutputDto fDto = new FileAttachmentOutputDto();
                fDto.setId(att.getId());
                fDto.setFileName(att.getFileName());
                fDto.setContentType(att.getContentType());
                return fDto;
            }).toList();
            dto.setFiles(fileDtos);

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
}