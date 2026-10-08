package com.spamer.config;

import com.spamer.domain.EndpointType;
import com.spamer.domain.ServiceEntity;
import com.spamer.domain.ServiceIntegrationKind;
import com.spamer.domain.UserEntity;
import com.spamer.domain.UserRole;
import com.spamer.domain.UserTier;
import com.spamer.repository.ServiceRepository;
import com.spamer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String victimBaseUrl;

    public DataInitializer(
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            PasswordEncoder passwordEncoder,
            @Value("${spamer.admin.password:admin}") String adminPassword,
            @Value("${spamer.victim.base-url:http://localhost:8080}") String victimBaseUrl) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.victimBaseUrl = victimBaseUrl;
    }

    @Override
    public void run(String... args) {
        ensureUser("admin", adminPassword, UserRole.ADMIN, UserTier.PRO, true);
        ensureUser("tester", "test123", UserRole.USER, UserTier.FREE, false);

        seedOtpServices();
    }

    private void seedOtpServices() {
        ensureRealService(
                "Yandex ID",
                ServiceIntegrationKind.YANDEX_PWL_OTP,
                EndpointType.OTP_REQUEST,
                "https://passport.yandex.ru",
                "/pwl-yandex/auth/add",
                null);
        ensureRealService(
                "Ozon",
                ServiceIntegrationKind.OZON_FAST_ENTRY,
                EndpointType.OTP_REQUEST,
                "https://www.ozon.ru",
                "/api/composer-api.bx/_action/fastEntry",
                "{\"phone\":\"{{phone_e164}}\",\"otpId\":0}");

        ensureRealService(
                "Korona Pay",
                ServiceIntegrationKind.KORONAPAY_OTP,
                EndpointType.OTP_REQUEST,
                "https://koronapay.com",
                "/transfers/online/api/users/otps",
                null);
        ensureRealService(
                "Start.ru",
                ServiceIntegrationKind.START_RU_OTP,
                EndpointType.OTP_REQUEST,
                "https://start.ru",
                "/api/v1/auth/otp",
                "{\"phone\":\"{{phone_e164}}\"}");
        ensureRealService(
                "Qlean",
                ServiceIntegrationKind.QLEAN_OTP,
                EndpointType.OTP_REQUEST,
                "https://qlean.ru",
                "/clients-api/v2/sms_codes/auth/request_code",
                "{\"phone\":\"{{phone}}\"}");
        ensureRealService(
                "MTS TV",
                ServiceIntegrationKind.MTS_TV_OTP,
                EndpointType.OTP_REQUEST,
                "https://prod.tvh.mts.ru",
                "/tvh-public-api-gateway/public/rest/general/send-code",
                null);
        ensureRealService(
                "Webbankir",
                ServiceIntegrationKind.WEBBANKIR_OTP,
                EndpointType.OTP_REQUEST,
                "https://ng-api.webbankir.com",
                "/user/v2/create",
                null);

        ensureMockService("Wildberries", "wildberries", EndpointType.OTP_REQUEST, "/otp");
        ensureMockService("Sber ID", "sber", EndpointType.OTP_REQUEST, "/otp");
        ensureMockService("VK ID", "vk", EndpointType.OTP_REQUEST, "/otp");
        ensureMockService("Avito", "avito", EndpointType.OTP_REQUEST, "/otp");
        ensureMockService("T-Bank", "tbank", EndpointType.OTP_REQUEST, "/otp");
        ensureMockService("Yandex Password Reset", "yandex", EndpointType.PASSWORD_RESET, "/password-reset");
        ensureMockService("Ozon Password Reset", "ozon", EndpointType.PASSWORD_RESET, "/password-reset");
    }

    private void ensureRealService(
            String name,
            ServiceIntegrationKind integration,
            EndpointType endpointType,
            String baseUrl,
            String endpointPath,
            String bodyTemplate) {
        serviceRepository.findByName(name).ifPresentOrElse(service -> {
            service.setIntegration(integration);
            service.setBaseUrl(baseUrl);
            service.setEndpointPath(endpointPath);
            service.setEndpointType(endpointType);
            service.setHttpMethod("POST");
            service.setRequestBodyTemplate(bodyTemplate);
            service.setEnabled(true);
            serviceRepository.save(service);
        }, () -> {
            ServiceEntity service = new ServiceEntity();
            service.setName(name);
            service.setIntegration(integration);
            service.setBaseUrl(baseUrl);
            service.setEndpointType(endpointType);
            service.setEndpointPath(endpointPath);
            service.setHttpMethod("POST");
            service.setRequestBodyTemplate(bodyTemplate);
            serviceRepository.save(service);
        });
    }

    private void ensureMockService(
            String name,
            String provider,
            EndpointType endpointType,
            String actionPath) {
        String path = "/api/victim/" + provider + actionPath;
        String body = endpointType == EndpointType.PASSWORD_RESET
                ? "{\"phone\":\"{{phone}}\",\"action\":\"reset\"}"
                : "{\"phone\":\"{{phone}}\",\"action\":\"otp\"}";

        serviceRepository.findByName(name).ifPresentOrElse(service -> {
            service.setIntegration(ServiceIntegrationKind.GENERIC_HTTP);
            service.setBaseUrl(victimBaseUrl);
            service.setEndpointPath(path);
            service.setEndpointType(endpointType);
            service.setHttpMethod("POST");
            service.setRequestBodyTemplate(body);
            service.setEnabled(true);
            serviceRepository.save(service);
        }, () -> {
            ServiceEntity service = new ServiceEntity();
            service.setName(name);
            service.setIntegration(ServiceIntegrationKind.GENERIC_HTTP);
            service.setBaseUrl(victimBaseUrl);
            service.setEndpointType(endpointType);
            service.setEndpointPath(path);
            service.setHttpMethod("POST");
            service.setRequestBodyTemplate(body);
            serviceRepository.save(service);
        });
    }

    private void ensureUser(
            String username,
            String password,
            UserRole role,
            UserTier tier,
            boolean syncPassword) {
        userRepository.findByUsername(username).ifPresentOrElse(user -> {
            if (syncPassword) {
                user.setPasswordHash(passwordEncoder.encode(password));
                user.setRole(role);
                user.setTier(tier);
                userRepository.save(user);
            }
        }, () -> {
            UserEntity user = new UserEntity();
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            user.setTier(tier);
            userRepository.save(user);
        });
    }
}
