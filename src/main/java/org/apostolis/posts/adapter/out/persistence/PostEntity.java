package org.apostolis.posts.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apostolis.comments.adapter.out.persistence.CommentEntity;
import org.apostolis.users.adapter.out.persistence.UserEntity;
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
@NoArgsConstructor
@Getter
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    Long post_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    UserEntity user;

    @Column(length=5000, nullable = false)
    String text;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    boolean isShared = false;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL , fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    Set<CommentEntity> post_comments = new HashSet<>();

    public PostEntity(UserEntity postCreator, String text, LocalDateTime createdAt){
        user = postCreator;
        this.text = text;
        this.createdAt = createdAt;
    }

    public void setShared(){
        isShared = true;
    }

    public void setUser(UserEntity creator){
        user = creator;
    }

}
