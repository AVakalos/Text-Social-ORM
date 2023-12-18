package org.apostolis.posts.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apostolis.comments.adapter.out.persistence.CommentEntity;
import org.apostolis.posts.domain.Post;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.domain.UserId;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// Hibernate ORM Entity for Posts
@Entity
@Table(name="posts")
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long post_id;

    @Column(name="user_id")
    private Long user_id;

    @Column(length=5000, nullable = false)
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Getter
    @Column(nullable = false)
    private boolean isShared = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL , fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    Set<CommentEntity> post_comments = new HashSet<>();

    private PostEntity(Long postCreator, String text, LocalDateTime createdAt){
        user_id = postCreator;
        this.text = text;
        this.createdAt = createdAt;
    }

    public void setShared(){
        isShared = true;
    }

    public void addComment(CommentEntity newComment){
        post_comments.add(newComment);
    }

    public Post mapToDomain(){
        return new Post(new PostId(post_id), new UserId(user_id), text, createdAt);
    }

    public static PostEntity mapToEntity(Post post){
        return new PostEntity(post.getUser().getValue(), post.getText(),post.getCreatedAt());
    }
}
