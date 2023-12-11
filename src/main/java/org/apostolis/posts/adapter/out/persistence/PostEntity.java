package org.apostolis.posts.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apostolis.comments.adapter.out.persistence.CommentEntity;
import org.apostolis.posts.domain.PostId;
import org.apostolis.users.adapter.out.persistence.UserEntity;
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
@NoArgsConstructor
@Getter
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    Long post_id;

    //@ManyToOne(fetch = FetchType.LAZY)
    //@JoinColumn(name="user_id")
    @Column(name="user_id")
    Long user_id;

    @Column(length=5000, nullable = false)
    String text;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    boolean isShared = false;

//    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL , fetch = FetchType.LAZY, orphanRemoval = true)
//    @Fetch(FetchMode.SUBSELECT)
//    Set<CommentEntity> post_comments = new HashSet<>();

    public PostEntity(Long postCreator, String text, LocalDateTime createdAt){
        user_id = postCreator;
        this.text = text;
        this.createdAt = createdAt;
    }

    public void setShared(){
        isShared = true;
    }

    public void setUser(Long creator){
        user_id = creator;
    }

}
