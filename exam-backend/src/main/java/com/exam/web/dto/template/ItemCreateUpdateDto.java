package com.exam.web.dto.template;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record ItemCreateUpdateDto(
        @NotBlank String title,
        String description,
        @NotNull @PositiveOrZero Double value,
        Long categoryId,
        List<Long> tagIds
) {}
