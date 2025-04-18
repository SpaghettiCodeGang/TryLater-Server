package com.spaghetticodegang.trylater.contact;

import com.spaghetticodegang.trylater.contact.enums.ContactStatus;
import com.spaghetticodegang.trylater.user.User;
import com.spaghetticodegang.trylater.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ContactRepositoryTest {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    private User requester;
    private User receiver;

    @BeforeEach
    void setup() {
        requester = userRepository.save(User.builder()
                .userName("requester")
                .displayName("Requester")
                .email("requester@example.com")
                .password("secure123")
                .imgPath("/assets/user.webp")
                .registrationDate(LocalDateTime.now())
                .build());

        receiver = userRepository.save(User.builder()
                .userName("receiver")
                .displayName("Receiver")
                .email("receiver@example.com")
                .password("secure123")
                .imgPath("/assets/user.webp")
                .registrationDate(LocalDateTime.now())
                .build());
    }

    @Test
    void shouldReturnTrue_whenContactExistsInRequestDirection() {
        contactRepository.save(Contact.builder()
                .requester(requester)
                .receiver(receiver)
                .contactStatus(ContactStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build());

        boolean exists = contactRepository.existsByUserIds(requester.getId(), receiver.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnTrue_whenContactExistsInReverseDirection() {
        contactRepository.save(Contact.builder()
                .requester(requester)
                .receiver(receiver)
                .contactStatus(ContactStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build());

        boolean exists = contactRepository.existsByUserIds(receiver.getId(), requester.getId());
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalse_whenNoContactExists() {
        boolean exists = contactRepository.existsByUserIds(requester.getId(), receiver.getId());
        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnFilteredContactsByUserIdAndStatus() {
        contactRepository.save(Contact.builder()
                .requester(requester)
                .receiver(receiver)
                .contactStatus(ContactStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build());

        contactRepository.save(Contact.builder()
                .requester(receiver)
                .receiver(requester)
                .contactStatus(ContactStatus.ACCEPTED)
                .requestDate(LocalDateTime.now())
                .build());

        var result = contactRepository.findByUserIdAndContactStatus(requester.getId(), ContactStatus.ACCEPTED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getContactStatus()).isEqualTo(ContactStatus.ACCEPTED);
        assertThat(result.getFirst().getRequester()).isEqualTo(receiver);
    }

    @Test
    void shouldFindContactsByRequesterIdAndStatus() {
        contactRepository.save(Contact.builder()
                .requester(requester)
                .receiver(receiver)
                .contactStatus(ContactStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build());

        var result = contactRepository.findByRequesterIdAndContactStatus(requester.getId(), ContactStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getRequester()).isEqualTo(requester);
    }

    @Test
    void shouldFindContactsByReceiverIdAndStatus() {
        contactRepository.save(Contact.builder()
                .requester(requester)
                .receiver(receiver)
                .contactStatus(ContactStatus.PENDING)
                .requestDate(LocalDateTime.now())
                .build());

        var result = contactRepository.findByReceiverIdAndContactStatus(receiver.getId(), ContactStatus.PENDING);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getReceiver()).isEqualTo(receiver);
    }

    @Test
    void shouldFindBlockedContactsByBlockedById() {
        contactRepository.save(Contact.builder()
                .requester(requester)
                .receiver(receiver)
                .contactStatus(ContactStatus.BLOCKED)
                .blockedBy(requester)
                .requestDate(LocalDateTime.now())
                .build());

        var result = contactRepository.findByBlockedByIdAndContactStatus(requester.getId(), ContactStatus.BLOCKED);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getBlockedBy()).isEqualTo(requester);
    }


}
