package com.exam.service.auth;

import com.exam.core.error.NotFoundException;
import com.exam.core.log.NoLogging;
import com.exam.model.auth.UserEntity;
import com.exam.model.enums.UserRole;
import com.exam.repository.auth.UserRepository;
import com.exam.core.config.Constants;
import com.exam.repository.specification.UserSpecification;
import com.exam.service.AbstractReadService;
import com.exam.web.dto.auth.UserCreateDto;
import com.exam.web.dto.auth.UserUpdateDto;
import com.exam.web.dto.filter.UserFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
public class UserService extends AbstractReadService<UserEntity, UserRepository> {
    private final PasswordEncoder passwordEncoder;
    private final RefreshSessionService refreshSessionService;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder, RefreshSessionService refreshSessionService) {
        super(repository, UserEntity.class);
        this.passwordEncoder = passwordEncoder;
        this.refreshSessionService = refreshSessionService;
    }

    @Transactional
    public void initDefaultUsers() {
        if (repository.count() == 0) {
            log.info("No users found. Creating default accounts...");
            createUser("Admin", "admin@exam.com", "Admin12!", UserRole.SUPERADMIN);
            createUser("User1", "user1@exam.com", "User123!", UserRole.USER);
            createUser("User2", "user2@exam.com", "User123!", UserRole.USER);
            log.info("Default users created successfully.");
        }
    }

    @Transactional(readOnly = true)
    public UserEntity validateAndGetUser(String username, String rawPassword) {
        UserEntity user = getByUsername(username);
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }

    @Transactional(readOnly = true)
    public UserEntity getByUsername(String username) {
        return repository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException(UserEntity.class, "username", username));
    }

    @Transactional(readOnly = true)
    public Page<UserEntity> getAll(UserFilter filter, Pageable pageable) {
        Specification<UserEntity> spec = UserSpecification.withFilters(filter);
        return repository.findAll(spec, pageable);
    }

    @Transactional
    public UserEntity createUser(String username, String email, String password, UserRole role) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be null or empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or empty");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role must not be null");
        }
        if (repository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (repository.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        validatePasswordStrength(password);

        UserEntity user = new UserEntity();
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(role);
        return repository.save(user);
    }

    @Transactional
    public UserEntity createUserByAdmin(UserCreateDto dto, UserRole currentUserRole) {
        checkRoleHierarchy(currentUserRole, dto.role(), "create");
        return createUser(dto.username(), dto.email(), dto.password(), dto.role());
    }

    @Transactional
    public UserEntity updateUser(Long id, UserUpdateDto dto, UserRole currentUserRole, String currentUsername) {
        UserEntity user = getById(id);
        boolean isSelf = user.getUsername().equalsIgnoreCase(currentUsername);

        if (!isSelf) {
            checkRoleHierarchy(currentUserRole, user.getRole(), "update");
            checkRoleHierarchy(currentUserRole, dto.role(), "assign");
        } else if (user.getRole() != dto.role()) {
            throw new IllegalArgumentException("You cannot change your own role");
        }

        user.setRole(dto.role());
        if (dto.password() != null && !dto.password().isBlank()) {
            applyPasswordChange(user, dto.password());
        }
        return repository.save(user);
    }

    @NoLogging
    @Transactional
    public void updateMyPassword(String currentUsername, String newPassword) {
        UserEntity user = getByUsername(currentUsername);
        applyPasswordChange(user, newPassword);
        repository.save(user);
    }

    @Transactional
    public void deleteUser(Long id, UserRole currentUserRole) {
        UserEntity user = getById(id);
        checkRoleHierarchy(currentUserRole, user.getRole(), "delete");
        refreshSessionService.revokeAllUserSessions(user.getId());
        repository.delete(user);
    }

    @Transactional
    public void revokeSessions(Long targetUserId, UserRole currentUserRole) {
        UserEntity user = getById(targetUserId);
        checkRoleHierarchy(currentUserRole, user.getRole(), "revoke sessions of");
        user.setSessionsValidFrom(Instant.now());
        repository.save(user);
        refreshSessionService.revokeAllUserSessions(user.getId());
    }

    public void validatePasswordStrength(String rawPassword) {
        if (rawPassword == null || !rawPassword.matches(Constants.PASSWORD_PATTERN)) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }
    }

    private void applyPasswordChange(UserEntity user, String newPassword) {
        validatePasswordStrength(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setSessionsValidFrom(Instant.now());
        refreshSessionService.revokeAllUserSessions(user.getId());
    }

    private void checkRoleHierarchy(UserRole currentUserRole, UserRole targetRole, String action) {
        if (targetRole == UserRole.SUPERADMIN) {
            throw new IllegalArgumentException("Nobody has permission to " + action + " a SUPERADMIN");
        }
        if (currentUserRole == UserRole.ADMIN && targetRole == UserRole.ADMIN) {
            throw new IllegalArgumentException("No permission to " + action + " an ADMIN");
        }
    }
}
