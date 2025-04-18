package com.spaghetticodegang.trylater.user;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * JPA entity representing a user of the system.
 * Implements {@link UserDetails} for Spring Security integration.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userName;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String imgPath;

    @Column(nullable = false)
    private LocalDateTime registrationDate;

    /**
     * Returns an empty list of authorities. This application does not use roles yet.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

    /**
     * Returns the unique username used by Spring Security for authentication.
     * In this case, it is the user's email.
     */
    @Override public String getUsername() { return email; }

    /**
     * Returns the actual username (used for display or registration).
     * Must be declared explicitly due to method name conflict with {@link UserDetails#getUsername()}.
     *
     * @return the internal username
     */    public String getUserName() {
        return userName;
    }
}
