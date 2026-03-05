package nl.novi.tickettracker.repositories;

import nl.novi.tickettracker.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
}