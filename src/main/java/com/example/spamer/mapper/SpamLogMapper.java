package com.example.spamer.mapper;

import com.example.spamer.domain.entity.ProxyEntity;
import com.example.spamer.domain.entity.SpamLogEntity;
import com.example.spamer.dto.response.SpamLogResponse;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface SpamLogMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "proxyIds", source = "proxies", qualifiedByName = "proxyIds")
    SpamLogResponse toResponse(SpamLogEntity entity);

    @Named("proxyIds")
    default Set<UUID> proxyIds(Set<ProxyEntity> proxies) {
        if (proxies == null) {
            return Set.of();
        }
        return proxies.stream().map(ProxyEntity::getId).collect(Collectors.toSet());
    }
}
