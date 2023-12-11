package org.apostolis.comments.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.apostolis.comments.domain.CommentId;
import org.apostolis.posts.adapter.out.persistence.PostEntity;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.apostolis.users.domain.UserId;

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

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name="post_id")
    @Column(name="post_id")
    Long post_id;

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name="user_id", nullable = false)
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
