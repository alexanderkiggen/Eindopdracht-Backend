package nl.novi.tickettracker.repositories;

import nl.novi.tickettracker.models.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Integer> {
}