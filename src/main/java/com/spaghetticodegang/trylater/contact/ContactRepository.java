package com.spaghetticodegang.trylater.contact;

import com.spaghetticodegang.trylater.contact.enums.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for accessing and managing contact entities in the database.
 */
public interface ContactRepository extends JpaRepository<Contact, Long> {

    /**
     * Checks whether a contact relationship exists between two users,
     * regardless of which user was the requester or receiver.
     *
     * @param userId1 the ID of the first user
     * @param userId2 the ID of the second user
     * @return true if a contact already exists between the two users, false otherwise
     */
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Contact c
            WHERE (c.requester.id = :userId1 AND c.receiver.id = :userId2)
               OR (c.requester.id = :userId2 AND c.receiver.id = :userId1)
            """)
    boolean existsByUserIds(Long userId1, Long userId2);

    /**
     * Finds all contacts for a given requesterId or receiverId.
     * Filters the contacts according to the given status.
     *
     * @param userId        the ID of the requester or receiver
     * @param contactStatus the contact status
     * @return a list of {@link Contact} entities.
     */
    @Query("""
            SELECT c
            FROM Contact c
            WHERE (c.requester.id = :userId OR c.receiver.id = :userId)
                AND c.contactStatus = :contactStatus
            """)
    List<Contact> findByUserIdAndContactStatus(Long userId, ContactStatus contactStatus);

    /**
     * Finds all contacts where the given user is the receiver and the contact has the specified status.
     *
     * @param receiverId    the ID of the receiver
     * @param contactStatus the status of the contact
     * @return a list of {@link Contact} entities
     */
    List<Contact> findByReceiverIdAndContactStatus(Long receiverId, ContactStatus contactStatus);

    /**
     * Finds all contacts where the given user is the requester and the contact has the specified status.
     *
     * @param requesterId   the ID of the requester
     * @param contactStatus the status of the contact
     * @return a list of {@link Contact} entities
     */
    List<Contact> findByRequesterIdAndContactStatus(Long requesterId, ContactStatus contactStatus);

    /**
     * Finds all blocked contacts where the given user is the one who performed the blocking.
     *
     * @param blockedById   the ID of the user who blocked the contact
     * @param contactStatus the status of the contact (should be BLOCKED)
     * @return a list of {@link Contact} entities
     */
    List<Contact> findByBlockedByIdAndContactStatus(Long blockedById, ContactStatus contactStatus);
}
