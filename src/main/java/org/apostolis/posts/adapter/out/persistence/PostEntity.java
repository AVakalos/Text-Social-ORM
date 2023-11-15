package org.apostolis.posts.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.apostolis.users.adapter.out.persistence.UserEntity;

import java.time.LocalDateTime;

@Entity
@Table(name="posts")
@NoArgsConstructor
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int post_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    UserEntity user;

    @Column(length=5000, nullable = false)
    String text;

    @Column(nullable = false)
    LocalDateTime createdAt;

    public PostEntity(UserEntity postCreator, String text, LocalDateTime createdAt){
        user = postCreator;
        this.text = text;
        this.createdAt = createdAt;
    }
}
