package nl.novi.tickettracker.controllers;

import jakarta.validation.Valid;
import nl.novi.tickettracker.dtos.UpdateUserProfileDto;
import nl.novi.tickettracker.dtos.UserInputDto;
import nl.novi.tickettracker.dtos.UserOutputDto;
import nl.novi.tickettracker.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("ALL")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserOutputDto> createUser(@Valid @RequestBody UserInputDto dto) {
        UserOutputDto createdUser = userService.createUser(dto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED); // Status code: 201 Created
    }

    @GetMapping
    public ResponseEntity<List<UserOutputDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers()); // Status code: 200 Ok
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserOutputDto> getUserByUsername(@PathVariable("username") String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username)); // Status code: 200 Ok
    }

    @PutMapping("/{username}/profile")
    public ResponseEntity<UserOutputDto> updateUserProfile(
            @PathVariable("username") String username,
            @Valid @RequestBody UpdateUserProfileDto dto) {

        UserOutputDto updatedUser = userService.updateUserProfile(username, dto);
        return ResponseEntity.ok(updatedUser); // Status code: 200 Ok
    }
}