package com.spaghetticodegang.trylater.recommendation.category;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity representing a category of a recommendation.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private CategoryType categoryType;
}
