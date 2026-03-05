package nl.novi.tickettracker.repositories;

import nl.novi.tickettracker.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Project, Integer> {
}