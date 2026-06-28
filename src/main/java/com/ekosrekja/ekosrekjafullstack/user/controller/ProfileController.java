package com.ekosrekja.ekosrekjafullstack.user.controller;


import com.ekosrekja.ekosrekjafullstack.admin.AdminRepository;
import com.ekosrekja.ekosrekjafullstack.user.dto.ChangePasswordRequest;
import com.ekosrekja.ekosrekjafullstack.user.dto.UserResponse;
import com.ekosrekja.ekosrekjafullstack.user.entity.User;
import com.ekosrekja.ekosrekjafullstack.user.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminRepository adminRepository;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(mapToUserResponse(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(mapToUserResponse(user));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateCurrentProfile(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody UserResponse userResponse) {
        User user = resolveCurrentUser(userId);

        return updateUserProfile(user, userResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(
            @PathVariable Long id,
            @RequestBody UserResponse userResponse) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return updateUserProfile(user, userResponse);
    }

    private ResponseEntity<UserResponse> updateUserProfile(User user, UserResponse userResponse) {

        if (userResponse.getFirstName() != null) {
            user.setFirstName(userResponse.getFirstName());
        }
        if (userResponse.getLastName() != null) {
            user.setLastName(userResponse.getLastName());
        }
        if (userResponse.getEmail() != null) {
            user.setEmail(userResponse.getEmail());
        }

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(mapToUserResponse(updatedUser));
    }

    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadCurrentProfilePicture(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam("file") MultipartFile file) {
        User user = resolveCurrentUser(userId);
        return updateProfilePicture(user, file);
    }

    @PostMapping(value = "/{id}/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponse> uploadProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return updateProfilePicture(user, file);
    }

    private ResponseEntity<UserResponse> updateProfilePicture(User user, MultipartFile file) {
        user.setProfilePictureUrl(storeProfilePicture(file));
        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(mapToUserResponse(updatedUser));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
        return ResponseEntity.ok("Profile deleted successfully");
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changeCurrentPassword(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestBody ChangePasswordRequest request) {
        User user = resolveCurrentUser(userId);

        return changeUserPassword(user, request);
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<String> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return changeUserPassword(user, request);
    }

    private ResponseEntity<String> changeUserPassword(User user, ChangePasswordRequest request) {

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("Password changed successfully");
    }

    private User resolveCurrentUser(Long userId) {
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePictureUrl(user.getProfilePictureUrl())
                .createdAt(user.getCreatedAt())
                .isAdmin(Boolean.TRUE.equals(user.getIsAdmin()) || adminRepository.existsByUserId(user.getId()))
                .build();
    }

    private String storeProfilePicture(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload file is required");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type");
        }

        String extension = extensionFrom(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;
        Path targetFolder = Paths.get(uploadDir).toAbsolutePath().normalize().resolve("profiles");
        Path target = targetFolder.resolve(filename).normalize();
        if (!target.startsWith(targetFolder)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name");
        }

        try {
            Files.createDirectories(targetFolder);
            file.transferTo(target);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save uploaded file", ex);
        }

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/profiles/")
                .path(filename)
                .toUriString();
    }

    private String extensionFrom(String filename) {
        if (filename == null) return "";
        String cleanName = Paths.get(filename).getFileName().toString();
        int dotIndex = cleanName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == cleanName.length() - 1) return "";
        return cleanName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }
}
