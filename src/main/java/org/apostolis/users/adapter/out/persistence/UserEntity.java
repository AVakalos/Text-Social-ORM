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

    private UserEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public static UserEntity mapToEntity(User user){
        return new UserEntity(user.getUsername(),user.getPassword(),user.getRole());
    }
    public User mapToDomain(){
        return new User(new UserId(user_id), username, password, role);
    }
    public User mapToDomain(Set<User> following_users){
        return new User(new UserId(user_id), username, password, role, following_users);
    }

    public void addFollowing(UserEntity to_follow){
        following.add(to_follow);
    }

    public void removeFollowing(UserEntity to_unfollow){
        following.remove(to_unfollow);
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
