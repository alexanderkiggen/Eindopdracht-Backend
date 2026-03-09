package nl.novi.tickettracker.repositories;

import nl.novi.tickettracker.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByTicketId(Integer ticketId);
}