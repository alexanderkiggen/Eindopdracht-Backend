package nl.novi.tickettracker.services;

import nl.novi.tickettracker.dtos.UpdateUserProfileDto;
import nl.novi.tickettracker.dtos.UserInputDto;
import nl.novi.tickettracker.dtos.UserOutputDto;
import nl.novi.tickettracker.exceptions.RecordNotFoundException;
import nl.novi.tickettracker.models.User;
import nl.novi.tickettracker.models.UserProfile;
import nl.novi.tickettracker.repositories.UserProfileRepository;
import nl.novi.tickettracker.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public UserService(UserRepository userRepository, UserProfileRepository userProfileRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public UserOutputDto createUser(UserInputDto dto) {
        UserProfile profile = new UserProfile();
        profile.setFirstname(dto.getFirstname());
        profile.setLastname(dto.getLastname());
        profile.setBio(dto.getBio());
        profile.setEmail(dto.getEmail());
        profile.setPhone(dto.getPhone());
        profile.setBirthdate(dto.getBirthdate());

        UserProfile savedProfile = userProfileRepository.save(profile);

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(dto.getPassword());
        user.setUserProfile(savedProfile);

        User savedUser = userRepository.save(user);

        return transferToDto(savedUser);
    }

    public List<UserOutputDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::transferToDto)
                .collect(Collectors.toList());
    }

    public UserOutputDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RecordNotFoundException("User with username '" + username + "' not found."));
        return transferToDto(user);
    }

    public UserOutputDto updateUserProfile(String username, UpdateUserProfileDto dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RecordNotFoundException("User with username '" + username + "' not found."));

        UserProfile profile = user.getUserProfile();

        if (profile == null) {
            throw new RecordNotFoundException("No profile found for user '" + username + "'.");
        }

        if (dto.getFirstname() != null) profile.setFirstname(dto.getFirstname());
        if (dto.getLastname() != null) profile.setLastname(dto.getLastname());
        if (dto.getBio() != null) profile.setBio(dto.getBio());
        if (dto.getEmail() != null) profile.setEmail(dto.getEmail());
        if (dto.getPhone() != null) profile.setPhone(dto.getPhone());
        if (dto.getBirthdate() != null) profile.setBirthdate(dto.getBirthdate());

        userProfileRepository.save(profile);

        return transferToDto(user);
    }

    private UserOutputDto transferToDto(User user) {
        UserOutputDto dto = new UserOutputDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());

        if (user.getUserProfile() != null) {
            dto.setFirstname(user.getUserProfile().getFirstname());
            dto.setLastname(user.getUserProfile().getLastname());
            dto.setBio(user.getUserProfile().getBio());
            dto.setEmail(user.getUserProfile().getEmail());
            dto.setPhone(user.getUserProfile().getPhone());
            dto.setBirthdate(user.getUserProfile().getBirthdate());
        }

        return dto;
    }
}