package nl.novi.tickettracker.repositories;

import nl.novi.tickettracker.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
}