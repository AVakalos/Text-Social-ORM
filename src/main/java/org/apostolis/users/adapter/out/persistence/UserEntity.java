package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apostolis.posts.adapter.out.persistence.PostEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
@NoArgsConstructor
@Getter
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, nullable = false)
    private Long user_id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 50, nullable = false)
    private String role;

    @JoinTable(name = "followers",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "following_id")})
    @ManyToMany(cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private Set<UserEntity> following = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL, mappedBy = "following")
    @Fetch(FetchMode.SUBSELECT)
    private Set<UserEntity> followers = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Fetch(FetchMode.SUBSELECT)
    private Set<PostEntity> user_posts = new HashSet<>();


    public UserEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public void addFollowing(UserEntity to_follow){

        boolean existed = following.add(to_follow);
        if(!existed){
            throw new IllegalArgumentException("You already follow this user");
        }
        to_follow.followers.add(this);
    }

    public void removeFollowing(UserEntity to_unfollow){
        boolean existed = following.remove(to_unfollow);
        if(!existed){
            throw new IllegalArgumentException("You were not following this user or user does not exist");
        }
        to_unfollow.followers.remove(this);
    }
}
