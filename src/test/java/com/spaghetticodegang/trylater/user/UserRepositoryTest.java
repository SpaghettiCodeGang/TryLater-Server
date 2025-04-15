package com.spaghetticodegang.trylater.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldFindUserByEmailOrUserName() {
        User user = User.builder()
                .userName("tester")
                .displayName("tester")
                .email("test@example.com")
                .password("secure123")
                .imgPath("/assets/user.webp")
                .registrationDate(LocalDateTime.now())
                .build();
        userRepository.save(user);

        var byEmail = userRepository.findByEmailOrUserName("test@example.com", "notUsed");
        var byUserName = userRepository.findByEmailOrUserName("notUsed", "tester");

        assertThat(byEmail).isPresent();
        assertThat(byUserName).isPresent();
    }

    @Test
    void shouldReturnTrue_whenEmailExists() {
        User user = User.builder()
                .userName("tester")
                .displayName("tester")
                .email("test@example.com")
                .password("secure123")
                .imgPath("/assets/user.webp")
                .registrationDate(LocalDateTime.now())
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByEmail("test@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("doesnotexist@example.com");
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrue_whenUserNameExists() {
        User user = User.builder()
                .userName("tester")
                .displayName("tester")
                .email("test@example.com")
                .password("secure123")
                .imgPath("/assets/user.webp")
                .registrationDate(LocalDateTime.now())
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByUserName("tester");
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenUserNameDoesNotExist() {
        boolean exists = userRepository.existsByUserName("nonexistent");
        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindUserBySearchTerm() {
        User user = User.builder()
                .userName("searchTest")
                .displayName("Search Test")
                .email("search@example.com")
                .password("secure123")
                .imgPath("/assets/user.webp")
                .registrationDate(LocalDateTime.now())
                .build();
        userRepository.save(user);

        var byUserName = userRepository.findByEmailOrUserName("search@example.com", "search@example.com");
        var byEmail = userRepository.findByEmailOrUserName("search@example.com", "search@example.com");

        assertThat(byUserName).isPresent();
        assertThat(byEmail).isPresent();
        assertThat(byUserName.get().getUserName()).isEqualTo("searchTest");
    }
}
