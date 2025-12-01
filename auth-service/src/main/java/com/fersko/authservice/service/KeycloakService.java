package com.fersko.authservice.service;

import com.fersko.authservice.config.KeycloakConfig;
import com.fersko.authservice.dto.LoginRequest;
import com.fersko.authservice.dto.RegisterRequest;
import com.fersko.authservice.dto.TokenResponse;
import com.fersko.authservice.dto.UserResponse;
import com.fersko.authservice.exception.AuthenticationException;
import com.fersko.authservice.exception.KeycloakException;
import com.fersko.authservice.exception.UserAlreadyExistsException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    private final Keycloak keycloakAdmin;
    private final KeycloakConfig keycloakConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    public UserResponse registerUser(RegisterRequest request) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(keycloakConfig.getRealm());
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> existingUsers = usersResource.search(request.email());
            if (!existingUsers.isEmpty()) {
                throw new UserAlreadyExistsException("User with email " + request.email() + " already exists");
            }

            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(request.email());
            user.setEmail(request.email());
            user.setFirstName(request.firstName());
            user.setLastName(request.lastName());
            user.setEmailVerified(false);

            Response response = usersResource.create(user);

            if (response.getStatus() != 201) {
                log.error("Failed to create user in Keycloak. Status: {}", response.getStatus());
                throw new KeycloakException("Failed to create user in Keycloak");
            }

            String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setTemporary(false);
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.password());

            usersResource.get(userId).resetPassword(credential);

            response.close();

            log.info("User registered successfully: {}", request.email());

            return new UserResponse(
                    userId,
                    request.email(),
                    request.firstName(),
                    request.lastName(),
                    false
            );

        } catch (UserAlreadyExistsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error registering user: {}", e.getMessage(), e);
            throw new KeycloakException("Error registering user", e);
        }
    }

    public TokenResponse login(LoginRequest request) {
        try {
            String tokenUrl = keycloakConfig.getServerUrl() + "/realms/" + keycloakConfig.getRealm() + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "password");
            body.add("client_id", keycloakConfig.getClientId());
            if (keycloakConfig.getClientSecret() != null && !keycloakConfig.getClientSecret().isEmpty()) {
                body.add("client_secret", keycloakConfig.getClientSecret());
            }
            body.add("username", request.email());
            body.add("password", request.password());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenData = response.getBody();
                log.info("User logged in successfully: {}", request.email());

                return new TokenResponse(
                        (String) tokenData.get("access_token"),
                        (String) tokenData.get("refresh_token"),
                        (String) tokenData.get("token_type"),
                        ((Number) tokenData.get("expires_in")).longValue(),
                        ((Number) tokenData.get("refresh_expires_in")).longValue()
                );
            }

            throw new AuthenticationException("Authentication failed");

        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("Invalid credentials for user: {}", request.email());
            throw new AuthenticationException("Invalid email or password");
        } catch (Exception e) {
            log.error("Error during login: {}", e.getMessage(), e);
            throw new AuthenticationException("Authentication failed", e);
        }
    }

    public TokenResponse refreshToken(String refreshToken) {
        try {
            String tokenUrl = keycloakConfig.getServerUrl() + "/realms/" + keycloakConfig.getRealm() + "/protocol/openid-connect/token";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "refresh_token");
            body.add("client_id", keycloakConfig.getClientId());
            if (keycloakConfig.getClientSecret() != null && !keycloakConfig.getClientSecret().isEmpty()) {
                body.add("client_secret", keycloakConfig.getClientSecret());
            }
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenData = response.getBody();
                log.info("Token refreshed successfully");

                return new TokenResponse(
                        (String) tokenData.get("access_token"),
                        (String) tokenData.get("refresh_token"),
                        (String) tokenData.get("token_type"),
                        ((Number) tokenData.get("expires_in")).longValue(),
                        ((Number) tokenData.get("refresh_expires_in")).longValue()
                );
            }

            throw new AuthenticationException("Token refresh failed");

        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest e) {
            log.error("Invalid refresh token");
            throw new AuthenticationException("Invalid or expired refresh token");
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage(), e);
            throw new AuthenticationException("Token refresh failed", e);
        }
    }

    public void logout(String refreshToken) {
        try {
            String logoutUrl = keycloakConfig.getServerUrl() + "/realms/" + keycloakConfig.getRealm() + "/protocol/openid-connect/logout";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", keycloakConfig.getClientId());
            if (keycloakConfig.getClientSecret() != null && !keycloakConfig.getClientSecret().isEmpty()) {
                body.add("client_secret", keycloakConfig.getClientSecret());
            }
            body.add("refresh_token", refreshToken);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

            restTemplate.exchange(
                    logoutUrl,
                    HttpMethod.POST,
                    entity,
                    Void.class
            );

            log.info("User logged out successfully");

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage(), e);
            throw new KeycloakException("Logout failed", e);
        }
    }

    public UserResponse getUserInfo(String userId) {
        try {
            RealmResource realmResource = keycloakAdmin.realm(keycloakConfig.getRealm());
            UsersResource usersResource = realmResource.users();

            UserRepresentation user = usersResource.get(userId).toRepresentation();

            return new UserResponse(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.isEmailVerified()
            );

        } catch (Exception e) {
            log.error("Error getting user info: {}", e.getMessage(), e);
            throw new KeycloakException("Error retrieving user information", e);
        }
    }
}
