package com.spaghetticodegang.trylater.recommendation.tag;

import com.spaghetticodegang.trylater.recommendation.taggroup.TagGroup;
import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity representing a tag of a tag group.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tagName;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_group_id")
    private TagGroup tagGroup;

}