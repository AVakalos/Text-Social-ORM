package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

// Entity for Hibernate ORM
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

    @Transient
    private Set<Long> following = new HashSet<>();

    @Transient
    private Set<Long> followers = new HashSet<>();

    @Transient
    private Set<Long> user_posts = new HashSet<>();


    public UserEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

//    public void addFollowing(Long to_follow){
//
//        boolean existed = following.add(to_follow);
//        if(!existed){
//            throw new IllegalArgumentException("You already follow this user");
//        }
//    }
//
//    public void removeFollowing(Long to_unfollow){
//        boolean existed = following.remove(to_unfollow);
//        if(!existed){
//            throw new IllegalArgumentException("You were not following this user or user does not exist");
//        }
//    }
//
//    public void addFollower(Long follower){
//
//        boolean existed = followers.add(follower);
//        if(!existed){
//            throw new IllegalArgumentException("Add Follower Failed");
//        }
//    }
//
//    public void removeFollower(Long follower){
//        boolean existed = followers.remove(follower);
//        if(!existed){
//            throw new IllegalArgumentException("remove follower failed");
//        }
//    }
}
