package org.apostolis.posts.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apostolis.comments.adapter.out.persistence.CommentEntity;
import org.apostolis.comments.domain.Comment;
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
    @Getter
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

    @Getter
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL , fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private Set<CommentEntity> post_comments = new HashSet<>();

    private PostEntity(Long postCreator, String text, LocalDateTime createdAt){
        user_id = postCreator;
        this.text = text;
        this.createdAt = createdAt;
    }

    public void setShared(){
        isShared = true;
    }

    public void addComment(Comment newComment){
        post_comments.add(CommentEntity.mapToEntity(newComment, this));
    }

//    public Post mapToDomain(){
//        return new Post(new PostId(post_id), new UserId(user_id), text, createdAt);
//    }

    public Post mapToDomain(Set<Comment> comments) {
        return new Post(new PostId(post_id), new UserId(user_id), text, createdAt, comments);
    }

    public static PostEntity mapToEntity(Post post){
        return new PostEntity(post.getUser().getValue(), post.getText(),post.getCreatedAt());
    }

}
