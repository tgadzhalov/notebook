package com.example.OnlineNotebook.services;

import com.example.OnlineNotebook.security.UserData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
public class AttendanceMicroserviceClient {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceMicroserviceClient.class);

    private final RestTemplate restTemplate;
    private final JwtTokenService jwtTokenService;
    private final String baseUrl;
    private final String endpoint;

    public AttendanceMicroserviceClient(
            RestTemplate restTemplate,
            JwtTokenService jwtTokenService,
            @Value("${attendance.microservice.base-url}") String baseUrl,
            @Value("${attendance.microservice.endpoint}") String endpoint) {
        this.restTemplate = restTemplate;
        this.jwtTokenService = jwtTokenService;
        this.baseUrl = baseUrl;
        this.endpoint = endpoint;
    }

    public ResponseEntity<String> postAttendance(UserData userData, Map<String, Object> requestBody) {
        return postAttendance(userData.getId(), requestBody);
    }

    public ResponseEntity<String> postAttendance(UUID userId, Map<String, Object> requestBody) {
        String token = jwtTokenService.generateToken(userId);
        String url = baseUrl + endpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        logger.debug("Sending POST request to attendance microservice: {}", url);
        logger.debug("Request headers: Authorization=Bearer <token>, Content-Type=application/json");

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            logger.info("Attendance microservice response: status={}", response.getStatusCode());
            return response;
        } catch (RestClientException e) {
            logger.error("Failed to communicate with attendance microservice: {}", e.getMessage(), e);
            throw e;
        }
    }

    public <T> ResponseEntity<String> postAttendance(UUID userId, T requestBody) {
        String token = jwtTokenService.generateToken(userId);
        String url = baseUrl + endpoint;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        HttpEntity<T> requestEntity = new HttpEntity<>(requestBody, headers);

        logger.debug("Sending POST request to attendance microservice: {}", url);
        logger.debug("Request headers: Authorization=Bearer <token>, Content-Type=application/json");

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            logger.info("Attendance microservice response: status={}", response.getStatusCode());
            return response;
        } catch (RestClientException e) {
            logger.error("Failed to communicate with attendance microservice: {}", e.getMessage(), e);
            throw e;
        }
    }

    public String getAttendanceUrl() {
        return baseUrl + endpoint;
    }
}
