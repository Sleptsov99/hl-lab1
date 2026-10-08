package com.spamer.admin;

import com.spamer.domain.EndpointType;
import com.spamer.domain.ServiceEntity;
import com.spamer.domain.UserEntity;
import com.spamer.domain.UserRole;
import com.spamer.domain.UserTier;
import com.spamer.repository.ServiceRepository;
import com.spamer.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    public AdminService(UserRepository userRepository, ServiceRepository serviceRepository) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<UserEntity> listUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public UserEntity updateUserTier(Long userId, UserTier tier) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setTier(tier);
        return userRepository.save(user);
    }

    @Transactional
    public UserEntity updateUserRole(Long userId, UserRole role) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public ServiceEntity createService(
            String name,
            String baseUrl,
            EndpointType endpointType,
            String endpointPath,
            String httpMethod,
            String requestBodyTemplate) {
        ServiceEntity service = new ServiceEntity();
        service.setName(name);
        service.setBaseUrl(baseUrl);
        service.setEndpointType(endpointType);
        service.setEndpointPath(endpointPath);
        service.setHttpMethod(httpMethod);
        service.setRequestBodyTemplate(requestBodyTemplate);
        return serviceRepository.save(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceEntity> listServices() {
        return serviceRepository.findAll();
    }
}
