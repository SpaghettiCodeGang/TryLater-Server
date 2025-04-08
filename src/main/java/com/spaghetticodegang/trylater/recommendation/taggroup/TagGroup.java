package com.spaghetticodegang.trylater.recommendation.taggroup;

import com.spaghetticodegang.trylater.recommendation.category.Category;
import com.spaghetticodegang.trylater.recommendation.tag.Tag;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a tag group of a category.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tag_groups")
public class TagGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tagGroupName;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "tagGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Tag> tags = new ArrayList<>();

}
