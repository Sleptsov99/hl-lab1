package com.example.spamer.mapper;

import com.example.spamer.domain.entity.ServiceEntity;
import com.example.spamer.dto.response.ServiceResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ServiceMapper {

    ServiceResponse toResponse(ServiceEntity entity);
}
