package com.exam.web.mapper.template;

import com.exam.model.template.ItemEntity;
import com.exam.web.dto.template.ItemCreateUpdateDto;
import com.exam.web.dto.template.ItemDto;
import com.exam.web.mapper.auth.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
        UserMapper.class,
        CategoryMapper.class,
        TagMapper.class
})
public interface ItemMapper {
    ItemDto toDto(ItemEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ItemEntity toEntity(ItemCreateUpdateDto dto);
}
