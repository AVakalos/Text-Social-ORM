package org.apostolis.posts.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import java.time.LocalDateTime;

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

    @Column(name="user_id")
    Long user_id;

    @Column(length=5000, nullable = false)
    String text;

    @Column(nullable = false)
    LocalDateTime createdAt;

    @Column(nullable = false)
    boolean isShared = false;

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
