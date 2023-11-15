package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import org.apostolis.posts.adapter.out.persistence.PostEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="users")
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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


    @JoinTable(name="links",
            joinColumns = {@JoinColumn(name = "user_id")},
            inverseJoinColumns = {@JoinColumn(name = "post_id")})
    @OneToMany(fetch = FetchType.LAZY)
    private Set<PostEntity> linkSharedPosts = new HashSet<>();


    public UserEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }


    public void addFollowing(UserEntity to_follow){
        following.add(to_follow);
    }

    public void removeFollowing(UserEntity to_unfollow){
        following.remove(to_unfollow);
    }

    public void addFollower(UserEntity follower){
        followers.add(follower);
    }

    public void removeFollower(UserEntity follower){
        followers.remove(follower);
    }

    public void addToSharedPosts(PostEntity post){
        linkSharedPosts.add(post);
    }


}
