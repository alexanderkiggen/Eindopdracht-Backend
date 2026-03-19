package nl.novi.tickettracker.controllers;

import nl.novi.tickettracker.models.User;
import nl.novi.tickettracker.models.UserProfile;
import nl.novi.tickettracker.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // Data voor de tests
    @BeforeEach
    void setUp() {
        UserProfile profile1 = new UserProfile();
        profile1.setEmail("johndoe@novi-education.nl");
        User user1 = new User();
        user1.setUsername("johndoe");
        user1.setPassword("0000");
        user1.setUserProfile(profile1);
        userRepository.save(user1);

        UserProfile profile2 = new UserProfile();
        profile2.setEmail("janedoe@novi-education.nl");
        profile2.setFirstname("Jane");
        profile2.setLastname("Doe");
        User user2 = new User();
        user2.setUsername("janedoe");
        user2.setPassword("0000");
        user2.setUserProfile(profile2);
        userRepository.save(user2);
    }

    @Test
    public void testCreateUser() throws Exception {

        // Arrange
        String jsonBody = """
                {
                    "username": "newuser",
                    "password": "0000",
                    "email": "newuser@novi-education.nl",
                    "firstname": "new",
                    "lastname": "user",
                    "birthdate": "2000-01-01T00:00:00"
                }
                """;

        // Act
        var result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    public void testGetAllUsers() throws Exception {

        // Act
        var result = mockMvc.perform(get("/users"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").exists());
    }

    @Test
    public void testGetUserByUsername() throws Exception {

        // Arrange
        String existingUsername = "johndoe";

        // Act
        var result = mockMvc.perform(get("/users/" + existingUsername));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(existingUsername));
    }

    @Test
    public void testUpdateUserProfile() throws Exception {

        // Arrange
        String usernameToUpdate = "janedoe";
        String jsonBody = """
                {
                    "firstname": "newFirstname",
                    "lastname": "newLastname"
                }
                """;

        // Act
        var result = mockMvc.perform(put("/users/" + usernameToUpdate + "/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("newFirstname"))
                .andExpect(jsonPath("$.lastname").value("newLastname"));
    }
}