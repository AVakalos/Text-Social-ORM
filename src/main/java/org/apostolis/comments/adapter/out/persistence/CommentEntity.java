package org.apostolis.comments.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// Entity class for Hibernate ORM
@Entity
@Table(name="comments")
@NoArgsConstructor
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    Long comment_id;

    @Column(name="post_id")
    Long post_id;

    @Column(name="user_id")
    Long commentCreator;

    @Column(length=1000, nullable = false)
    String text;

    @Column(nullable = false)
    LocalDateTime createdAt;

    public CommentEntity(Long post, Long commentCreator, String text, LocalDateTime createdAt){
        this.post_id = post;
        this.commentCreator = commentCreator;
        this.text = text;
        this.createdAt = createdAt;
    }

    public void setPost(Long post){
        this.post_id = post;
    }
}
