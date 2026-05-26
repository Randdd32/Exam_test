package com.exam.web.dto.template;
import com.exam.web.dto.auth.UserDto;
import java.time.Instant;
import java.util.List;

public record ItemDto(
        Long id,
        String title,
        String description,
        Double value,
        UserDto author,
        CategoryDto category,
        List<TagDto> tags,
        Instant createdAt,
        Instant updatedAt
) {}
