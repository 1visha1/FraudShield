package com.securepay.auth.config;

import com.securepay.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationServerConfigTest {

    private final AuthorizationServerConfig config = new AuthorizationServerConfig();

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {

        PasswordEncoder encoder = config.passwordEncoder();

        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void passwordEncoder_ShouldEncodePassword() {

        PasswordEncoder encoder = config.passwordEncoder();

        String encoded = encoder.encode("password");

        assertNotEquals("password", encoded);
        assertTrue(encoder.matches("password", encoded));
    }

    @Test
    void authorizationServerSettings_ShouldReturnIssuer() {

        AuthorizationServerSettings settings =
                config.authorizationServerSettings();

        assertNotNull(settings);
        assertEquals("http://authorization-server:9000",
                settings.getIssuer());
    }
}