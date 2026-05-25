package com.exam.web.controller.auth;

import com.exam.core.config.Constants;
import com.exam.core.log.NoLogging;
import com.exam.model.enums.UserRole;
import com.exam.service.auth.UserService;
import com.exam.web.dto.auth.UpdatePasswordDto;
import com.exam.web.dto.auth.UserCreateDto;
import com.exam.web.dto.auth.UserDto;
import com.exam.web.dto.auth.UserUpdateDto;
import com.exam.web.dto.filter.UserFilter;
import com.exam.web.dto.pagination.PageDto;
import com.exam.web.mapper.auth.UserMapper;
import com.exam.web.mapper.pagination.PageDtoMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Constants.API_URL + "/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserDto getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userMapper.toDto(userService.getByUsername(username));
    }

    @NoLogging
    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateMyPassword(@Valid @RequestBody UpdatePasswordDto dto, Authentication authentication) {
        String username = authentication.getName();
        userService.updateMyPassword(username, dto.newPassword());
    }

    @NoLogging
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @GetMapping
    public PageDto<UserDto> getAllUsers(
            @ModelAttribute UserFilter filter,
            @PageableDefault(size = Constants.DEFAULT_PAGE_SIZE, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return PageDtoMapper.toDto(userService.getAll(filter, pageable), userMapper::toDto);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userMapper.toDto(userService.getById(id));
    }

    @NoLogging
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserCreateDto dto, Authentication authentication) {
        UserRole currentUserRole = extractRole(authentication);
        return userMapper.toDto(userService.createUserByAdmin(dto, currentUserRole));
    }

    @NoLogging
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto dto, Authentication authentication) {
        UserRole currentUserRole = extractRole(authentication);
        String currentUsername = authentication.getName();

        return userMapper.toDto(userService.updateUser(id, dto, currentUserRole, currentUsername));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id, Authentication authentication) {
        String currentUsername = authentication.getName();
        UserRole currentUserRole = extractRole(authentication);

        if (userService.getByUsername(currentUsername).getId().equals(id)) {
            throw new IllegalArgumentException("You cannot delete your own account");
        }

        userService.deleteUser(id, currentUserRole);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    @PostMapping("/{id}/revoke-sessions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeUserSessions(@PathVariable Long id, Authentication authentication) {
        UserRole currentUserRole = extractRole(authentication);
        userService.revokeSessions(id, currentUserRole);
    }

    private UserRole extractRole(Authentication authentication) {
        String roleStr = authentication.getAuthorities().iterator().next().getAuthority();
        return UserRole.valueOf(roleStr.replace("ROLE_", ""));
    }
}
