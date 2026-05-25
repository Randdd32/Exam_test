package com.exam.web.mapper.auth;

import com.exam.model.auth.UserEntity;
import com.exam.web.dto.auth.UserDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    UserDto toDto(UserEntity entity);
}
