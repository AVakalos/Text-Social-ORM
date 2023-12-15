package org.apostolis.users.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apostolis.users.domain.User;
import org.apostolis.users.domain.UserId;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.HashSet;
import java.util.Set;

// Entity for Hibernate ORM
@Entity
@Table(name="users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    public UserEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public User mapToDTO(){
        return new User(username, password, role);
    }

    public void addFollowing(UserEntity to_follow){

        boolean existed = following.add(to_follow);
        if(!existed){
            throw new IllegalArgumentException("You already follow user: " + to_follow.getUser_id());
        }
    }

    public void removeFollowing(UserEntity to_unfollow){
        boolean existed = following.remove(to_unfollow);
        if(!existed){
            throw new IllegalArgumentException("You were not following user: "+to_unfollow.getUser_id());
        }
    }

//    public void addFollower(UserEntity follower){
//
//        boolean existed = followers.add(follower);
//        if(!existed){
//            throw new IllegalArgumentException("Add Follower Failed");
//        }
//    }
//
//    public void removeFollower(UserEntity follower){
//        boolean existed = followers.remove(follower);
//        if(!existed){
//            throw new IllegalArgumentException("remove follower failed");
//        }
//    }
}
