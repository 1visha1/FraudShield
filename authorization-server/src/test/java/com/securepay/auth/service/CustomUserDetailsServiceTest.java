package com.securepay.auth.service;

import com.securepay.auth.entity.User;
import com.securepay.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_ShouldReturnUser_WhenUserExists() {
        // Arrange
        User user = User.builder()
                .username("john")
                .password("password123")
                .roles("ROLE_CUSTOMER")
                .enabled(true)
                .customerId("CUST001")
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        // Act
        UserDetails result = customUserDetailsService.loadUserByUsername("john");

        // Assert
        assertNotNull(result);
        assertEquals("john", result.getUsername());
        assertEquals("password123", result.getPassword());
        assertTrue(result.isEnabled());
        assertEquals(1, result.getAuthorities().size());
        assertEquals("ROLE_CUSTOMER",
                result.getAuthorities().iterator().next().getAuthority());

        verify(userRepository, times(1)).findByUsername("john");
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void loadUserByUsername_ShouldThrowUsernameNotFoundException_WhenUserDoesNotExist() {
        // Arrange
        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("john")
        );

        assertEquals("User not found with username: john", exception.getMessage());

        verify(userRepository, times(1)).findByUsername("john");
        verifyNoMoreInteractions(userRepository);
    }
}