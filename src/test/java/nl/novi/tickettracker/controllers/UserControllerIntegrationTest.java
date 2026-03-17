package nl.novi.tickettracker.controllers;

import nl.novi.tickettracker.dtos.UpdateUserProfileDto;
import nl.novi.tickettracker.dtos.UserInputDto;
import nl.novi.tickettracker.dtos.UserOutputDto;
import nl.novi.tickettracker.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    public void testCreateUser() throws Exception {

        // Arrange
        String jsonBody = """
                {
                    "username": "johndoe",
                    "password": "0000",
                    "email": "johndoe@novi-education.nl",
                    "firstname": "John",
                    "lastname": "Doe",
                    "birthdate": "2000-01-01T00:00:00"
                }
                """;

        UserOutputDto output = new UserOutputDto();
        output.setUsername("johndoe");

        when(userService.createUser(any(UserInputDto.class))).thenReturn(output);

        // Act
        var result = mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    public void testGetAllUsers() throws Exception {

        // Arrange
        UserOutputDto user1 = new UserOutputDto();
        user1.setUsername("user1");

        when(userService.getAllUsers()).thenReturn(List.of(user1));

        // Act
        var result = mockMvc.perform(get("/users"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"));
    }

    @Test
    public void testGetUserByUsername() throws Exception {

        // Arrange
        UserOutputDto user = new UserOutputDto();
        user.setUsername("janedoe");

        when(userService.getUserByUsername("janedoe")).thenReturn(user);

        // Act
        var result = mockMvc.perform(get("/users/janedoe"));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("janedoe"));
    }

    @Test
    public void testUpdateUserProfile() throws Exception {

        // Arrange
        String jsonBody = """
                {
                    "firstname": "John2",
                    "lastname": "Doe2"
                }
                """;

        UserOutputDto updated = new UserOutputDto();
        updated.setFirstname("John2");

        when(userService.updateUserProfile(eq("tester"), any(UpdateUserProfileDto.class))).thenReturn(updated);

        // Act
        var result = mockMvc.perform(put("/users/tester/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("John2"));
    }
}