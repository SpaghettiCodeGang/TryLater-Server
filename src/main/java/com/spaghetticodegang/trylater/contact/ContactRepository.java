package com.spaghetticodegang.trylater.contact;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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


}
