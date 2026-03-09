package nl.novi.tickettracker.repositories;

import nl.novi.tickettracker.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
    List<Ticket> findByProjectId(Integer projectId);
}