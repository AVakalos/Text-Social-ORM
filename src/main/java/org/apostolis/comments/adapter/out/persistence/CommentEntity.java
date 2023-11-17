package org.apostolis.comments.adapter.out.persistence;


import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apostolis.posts.adapter.out.persistence.PostEntity;
import org.apostolis.users.adapter.out.persistence.UserEntity;
import org.apostolis.users.domain.User;

import java.time.LocalDateTime;

@Entity
@Table(name="comments")
@NoArgsConstructor
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="post_id")
    PostEntity post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    UserEntity commentCreator;

    @Column(length=1000, nullable = false)
    String text;

    @Column(nullable = false)
    LocalDateTime createdAt;

    public CommentEntity(PostEntity post, UserEntity commentCreator, String text, LocalDateTime createdAt){
        this.post = post;
        this.commentCreator = commentCreator;
        this.text = text;
        this.createdAt = createdAt;
    }

    public void setPost(PostEntity post){
        this.post = post;
    }
}
