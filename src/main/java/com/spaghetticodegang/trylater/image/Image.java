package com.spaghetticodegang.trylater.image;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity representing an image.
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "images")
public class Image {

    @Id
    String imageId;
}
