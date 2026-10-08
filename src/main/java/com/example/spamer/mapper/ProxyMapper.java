package com.example.spamer.mapper;

import com.example.spamer.domain.entity.ProxyEntity;
import com.example.spamer.dto.response.ProxyResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProxyMapper {

    @Mapping(target = "providerId", source = "provider.id")
    ProxyResponse toResponse(ProxyEntity entity);
}
