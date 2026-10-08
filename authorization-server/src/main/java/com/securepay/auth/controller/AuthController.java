package com.fraudshield.auth.controller;

import com.fraudshield.auth.dto.ErrorResponse;
import com.fraudshield.auth.dto.LoginRequest;
import com.fraudshield.auth.dto.LoginResponse;
import com.fraudshield.auth.dto.RegistrationRequest;
import com.fraudshield.auth.entity.User;
import com.fraudshield.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContext;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final RegisteredClientRepository registeredClientRepository;
    private final OAuth2AuthorizationService authorizationService;
    private final OAuth2TokenGenerator<?> tokenGenerator;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationServerSettings authorizationServerSettings;

    private static final String CLIENT_ID = "fraudshield-client";

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Validated RegistrationRequest registrationRequest) {
        if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            return buildErrorResponse(HttpStatus.CONFLICT, "Username already exists.", "/api/auth/register");
        }

        User newUser = User.builder()
                .username(registrationRequest.getUsername())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .customerId(registrationRequest.getCustomerId())
                .roles("ROLE_USER")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(newUser);

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Validated LoginRequest loginRequest) {
        try {
            Authentication usernamePasswordAuthentication = new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );
            Authentication authenticatedUser = authenticationManager.authenticate(usernamePasswordAuthentication);

            RegisteredClient registeredClient = registeredClientRepository.findByClientId(CLIENT_ID);
            if (registeredClient == null) {
                log.error("RegisteredClient with ID '{}' not found for custom login endpoint.", CLIENT_ID);
                return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Authorization client not configured.", "/api/auth/login");
            }

            // Build AuthorizationServerContext manually — AuthorizationServerContextHolder
            // is only populated by the OAuth2 filter chain, not custom endpoints
            AuthorizationServerContext authorizationServerContext = new AuthorizationServerContext() {
                @Override
                public String getIssuer() {
                    return authorizationServerSettings.getIssuer() != null
                            ? authorizationServerSettings.getIssuer()
                            : "http://localhost:9000";
                }

                @Override
                public AuthorizationServerSettings getAuthorizationServerSettings() {
                    return authorizationServerSettings;
                }
            };

            OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                    .principalName(authenticatedUser.getName())
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD)
                    .authorizedScopes(registeredClient.getScopes())
                    .attribute(Authentication.class.getName(), authenticatedUser)
                    .build();

            DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                    .registeredClient(registeredClient)
                    .principal(authenticatedUser)
                    .authorizationServerContext(authorizationServerContext)
                    .authorization(authorization)
                    .authorizedScopes(registeredClient.getScopes())
                    .authorizationGrantType(AuthorizationGrantType.PASSWORD);

            // Generate Access Token
            // tokenGenerator returns a Jwt, not OAuth2AccessToken — wrap it manually
            OAuth2TokenContext accessTokenContext = tokenContextBuilder
                    .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                    .build();

            OAuth2Token generatedAccessToken = tokenGenerator.generate(accessTokenContext);
            if (generatedAccessToken == null) {
                log.error("Failed to generate access token for user: {}", loginRequest.getUsername());
                return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate access token.", "/api/auth/login");
            }

            OAuth2AccessToken accessToken = new OAuth2AccessToken(
                    OAuth2AccessToken.TokenType.BEARER,
                    generatedAccessToken.getTokenValue(),
                    generatedAccessToken.getIssuedAt(),
                    generatedAccessToken.getExpiresAt(),
                    registeredClient.getScopes()
            );

            // Generate Refresh Token
            OAuth2RefreshToken refreshToken = null;
            if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
                OAuth2TokenContext refreshTokenContext = tokenContextBuilder
                        .tokenType(OAuth2TokenType.REFRESH_TOKEN)
                        .build();
                OAuth2Token generatedRefreshToken = tokenGenerator.generate(refreshTokenContext);
                if (generatedRefreshToken instanceof OAuth2RefreshToken) {
                    refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
                }
            }

            // Save the complete authorization
            OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.from(authorization)
                    .accessToken(accessToken);
            if (refreshToken != null) {
                authorizationBuilder.refreshToken(refreshToken);
            }
            authorizationService.save(authorizationBuilder.build());

            return ResponseEntity.ok(LoginResponse.builder()
                    .accessToken(accessToken.getTokenValue())
                    .refreshToken(refreshToken != null ? refreshToken.getTokenValue() : null)
                    .tokenType(accessToken.getTokenType().getValue())
                    .expiresIn(accessToken.getExpiresAt().getEpochSecond() - accessToken.getIssuedAt().getEpochSecond())
                    .scope(String.join(" ", accessToken.getScopes()))
                    .build());

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", loginRequest.getUsername());
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid username or password.", "/api/auth/login");
        } catch (AuthenticationException e) {
            log.error("Authentication error for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication failed: " + e.getMessage(), "/api/auth/login");
        } catch (Exception e) {
            log.error("Unexpected error during login for user {}: {}", loginRequest.getUsername(), e.getMessage(), e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred during login.", "/api/auth/login");
        }
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.builder()
                        .timestamp(LocalDateTime.now())
                        .status(status.value())
                        .error(status.getReasonPhrase())
                        .message(message)
                        .path(path)
                        .build());
    }
}