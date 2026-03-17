package nl.novi.tickettracker.services;

import nl.novi.tickettracker.dtos.UpdateUserProfileDto;
import nl.novi.tickettracker.dtos.UserInputDto;
import nl.novi.tickettracker.dtos.UserOutputDto;
import nl.novi.tickettracker.exceptions.RecordNotFoundException;
import nl.novi.tickettracker.models.User;
import nl.novi.tickettracker.models.UserProfile;
import nl.novi.tickettracker.repositories.UserProfileRepository;
import nl.novi.tickettracker.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserService userService;

    @Test
    public void testCreateUser_Success() {
        // Arrange
        UserInputDto inputDto = new UserInputDto();
        inputDto.setUsername("johndoe");
        inputDto.setPassword("0000");
        inputDto.setFirstname("John");
        inputDto.setLastname("Doe");
        inputDto.setBio("Bio etc...");
        inputDto.setEmail("johndoe@novi-education.nl");
        inputDto.setPhone("0612345678");
        inputDto.setBirthdate(LocalDateTime.now());

        UserProfile savedProfile = new UserProfile();
        savedProfile.setId(1);
        savedProfile.setFirstname("John");
        savedProfile.setEmail("johndoe@novi-education.nl");

        User savedUser = new User();
        savedUser.setId(10);
        savedUser.setUsername("johndoe");
        savedUser.setUserProfile(savedProfile);

        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(savedProfile);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserOutputDto result = userService.createUser(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals(10, result.getId());
        assertEquals("johndoe", result.getUsername());
        assertEquals("John", result.getFirstname());
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testGetAllUsers_Success() {
        // Arrange
        UserProfile profile1 = new UserProfile();
        profile1.setFirstname("john");

        User user1 = new User();
        user1.setId(1);
        user1.setUsername("johndoe");
        user1.setUserProfile(profile1);

        User user2 = new User();
        user2.setId(2);
        user2.setUsername("janedoe");

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        // Act
        List<UserOutputDto> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        assertEquals("john", result.get(0).getFirstname());
        assertNull(result.get(1).getFirstname());
    }

    @Test
    public void testGetUserByUsername_Success() {
        // Arrange
        User user = new User();
        user.setId(5);
        user.setUsername("johndoe");
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));

        // Act
        UserOutputDto result = userService.getUserByUsername("johndoe");

        // Assert
        assertEquals(5, result.getId());
        assertEquals("johndoe", result.getUsername());
    }

    @Test
    public void testGetUserByUsername_NotFound() {
        // Arrange
        when(userRepository.findByUsername("onbekend")).thenReturn(Optional.empty());

        // Act
        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> userService.getUserByUsername("onbekend"));

        // Assert
        assertEquals("User with username 'onbekend' not found.", exception.getMessage());
    }

    @Test
    public void testUpdateUserProfile_Success_AllFields() {
        // Arrange
        UpdateUserProfileDto dto = new UpdateUserProfileDto();
        dto.setFirstname("NieuweVoornaam");
        dto.setLastname("NieuweAchternaam");
        dto.setBio("Nieuwe bio etc...");
        dto.setEmail("nieuw@email.nl");
        dto.setPhone("0612345678");
        dto.setBirthdate(LocalDateTime.now());

        UserProfile profile = new UserProfile();
        User user = new User();
        user.setUsername("johndoe");
        user.setUserProfile(profile);

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));

        // Act
        UserOutputDto result = userService.updateUserProfile("johndoe", dto);

        // Assert
        assertEquals("NieuweVoornaam", result.getFirstname());
        assertEquals("NieuweAchternaam", result.getLastname());
        assertEquals("Nieuwe bio etc...", result.getBio());
        verify(userProfileRepository, times(1)).save(profile);
    }

    @Test
    public void testUpdateUserProfile_UserNotFound() {
        // Arrange
        UpdateUserProfileDto dto = new UpdateUserProfileDto();
        when(userRepository.findByUsername("onbekend")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RecordNotFoundException.class, () -> userService.updateUserProfile("onbekend", dto));
    }

    @Test
    public void testUpdateUserProfile_ProfileNotFound() {
        // Arrange
        User userWithoutProfile = new User();
        userWithoutProfile.setUsername("geenprofiel");

        when(userRepository.findByUsername("geenprofiel")).thenReturn(Optional.of(userWithoutProfile));
        UpdateUserProfileDto dto = new UpdateUserProfileDto();

        // Act & Assert
        RecordNotFoundException exception = assertThrows(RecordNotFoundException.class, () -> userService.updateUserProfile("geenprofiel", dto));
        assertEquals("No profile found for user 'geenprofiel'.", exception.getMessage());
    }

    @Test
    public void testUpdateUserProfile_Success_NoFieldsProvided() {
        // Arrange
        UpdateUserProfileDto dto = new UpdateUserProfileDto();

        UserProfile profile = new UserProfile();
        profile.setFirstname("John");
        profile.setLastname("Doe");
        profile.setBio("Bio etc...");
        profile.setEmail("johndoe@novi-education.nl");
        profile.setPhone("0612345678");
        profile.setBirthdate(LocalDateTime.of(2000, 1, 1, 0, 0));

        User user = new User();
        user.setUsername("johndoe");
        user.setUserProfile(profile);

        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));

        // Act
        UserOutputDto result = userService.updateUserProfile("johndoe", dto);

        // Assert
        assertEquals("John", result.getFirstname());
        assertEquals("Doe", result.getLastname());
        assertEquals("Bio etc...", result.getBio());
        assertEquals("johndoe@novi-education.nl", result.getEmail());
        assertEquals("0612345678", result.getPhone());
        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 0), result.getBirthdate());

        verify(userProfileRepository, times(1)).save(profile);
    }
}