package com.example.spamer.mapper;

import com.example.spamer.domain.entity.SubscriptionEntity;
import com.example.spamer.dto.response.SubscriptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "serviceId", source = "service.id")
    SubscriptionResponse toResponse(SubscriptionEntity entity);
}
