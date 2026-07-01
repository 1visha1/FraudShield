package com.securepay.auth.repository;

import com.securepay.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @Test
    void shouldReturnUserWhenUsernameExists() {

        User user = User.builder()
                .username("john")
                .password("password")
                .customerId("CUST001")
                .roles("ROLE_USER")
                .enabled(true)
                .build();

        when(userRepository.findByUsername("john"))
                .thenReturn(Optional.of(user));

        Optional<User> result = userRepository.findByUsername("john");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("john");
    }

    @Test
    void shouldReturnEmptyWhenUsernameDoesNotExist() {

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        Optional<User> result = userRepository.findByUsername("unknown");

        assertThat(result).isEmpty();
    }
}