package nl.novi.tickettracker.controllers;

import jakarta.validation.Valid;
import nl.novi.tickettracker.dtos.UpdateUserProfileDto;
import nl.novi.tickettracker.dtos.UserOutputDto;
import nl.novi.tickettracker.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Postman Request: [User] Get All Users
    @GetMapping
    public ResponseEntity<List<UserOutputDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers()); // Status code: 200 Ok
    }

    // Postman Request: [User] Get User By Username
    @GetMapping("/{username}")
    public ResponseEntity<UserOutputDto> getUserByUsername(@PathVariable("username") String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username)); // Status code: 200 Ok
    }

    // Postman Request: [User] Update User Profile
    @PutMapping("/{username}/profile")
    public ResponseEntity<UserOutputDto> updateUserProfile(
            @PathVariable("username") String username,
            @Valid @RequestBody UpdateUserProfileDto dto) {

        UserOutputDto updatedUser = userService.updateUserProfile(username, dto);
        return ResponseEntity.ok(updatedUser); // Status code: 200 Ok
    }
}