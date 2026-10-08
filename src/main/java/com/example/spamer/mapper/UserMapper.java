package com.example.spamer.mapper;

import com.example.spamer.domain.entity.UserEntity;
import com.example.spamer.dto.response.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(UserEntity entity);
}
