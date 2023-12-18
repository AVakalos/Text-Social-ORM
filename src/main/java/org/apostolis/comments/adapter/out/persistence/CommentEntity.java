package org.apostolis.comments.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apostolis.comments.domain.Comment;
import org.apostolis.posts.adapter.out.persistence.PostEntity;

import java.time.LocalDateTime;

// Entity class for Hibernate ORM
@Entity
@Table(name="comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long comment_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="post_id")
    private PostEntity post;

    @Column(name="user_id")
    private Long commentCreator;

    @Column(length=1000, nullable = false)
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private CommentEntity(PostEntity post, Long commentCreator, String text, LocalDateTime createdAt){
        this.post = post;
        this.commentCreator = commentCreator;
        this.text = text;
        this.createdAt = createdAt;
    }

    public static CommentEntity mapToEntity(Comment comment, PostEntity postEntity){
        return new CommentEntity(
                postEntity,
                comment.getUser().getValue(),
                comment.getText(),
                comment.getCreatedAt());
    }
}
