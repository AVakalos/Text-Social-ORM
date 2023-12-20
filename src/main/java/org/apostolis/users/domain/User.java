package org.apostolis.users.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apostolis.exception.AuthenticationException;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.security.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Getter
@EqualsAndHashCode(exclude = {"following_users", "followers"})
public class User{
    private UserId id;
    private final String username;
    private final String password;
    private final String role;
    private final Set<User> following_users;
    private final Set<User> followers = new HashSet<>();

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    public User(String username, String password, String role){
        this.username = username;
        this.password = password;
        this.role = role;
        following_users = new HashSet<>();
    }

    public User(UserId user_id, String username, String password, String role){
        this.id = user_id;
        this.username = username;
        this.password = password;
        this.role = role;
        following_users = new HashSet<>();
    }

    public User(UserId user_id, String username, String password, String role, Set<User> following_users){
        this.id = user_id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.following_users = following_users;
    }

    public boolean checkPassword(String insertedPassword, PasswordEncoder passwordEncoder){
        try {
            return passwordEncoder.checkPassword(insertedPassword, password);
        }catch (Exception e){
            logger.error("Check password: "+e.getMessage());
            return false;
        }
    }

    public void authenticateUser(String token, TokenManager tokenManager) throws AuthenticationException{
        boolean isTokenValid = tokenManager.validateToken(token);
        if (!isTokenValid){
            logger.error("Token is invalid");
            throw new AuthenticationException("Authentication token is invalid");
        }
    }

    public void addFollowingUser(User followingUser){
        if(followingUser.getUsername().equals(this.username)){
            throw new IllegalArgumentException("You can't follow yourself");
        } else if (following_users.contains(followingUser)) {
            throw new IllegalArgumentException("User:"+this.username+" already following: "+ followingUser.username);
        }
        following_users.add(followingUser);
    }

    public void removeFollowingUser(User followingUser){
//        for(User following_user: following_users){
//            if(Objects.equals(followingUser.following_users, following_user.following_users)){
//                following_users.remove(followingUser);
//                return;
//            }
//            throw new IllegalArgumentException("User:"+followingUser.username+" was not following: "+ this.username);
//        }
//        following_users.forEach((c)->System.out.println("FOL: "+c.equals(followingUser)));
//        System.out.println("Contains "+following_users.contains(followingUser));
//        System.out.println("Remove following user "+followingUser.getId());
        if (!following_users.contains(followingUser)) {
            throw new IllegalArgumentException("User:"+this.username+" was not following: "+ followingUser.username);
        }
        following_users.remove(followingUser);

    }

//    public void addFollower(User follower){
//        if(!followers.contains(follower)){
//            followers.add(follower);
//        }else{
//            throw new IllegalArgumentException("User:"+this.username+" already in followers of: "+ follower.username);
//        }
//
//    }
//
//    public void removeFollower(User follower){
//        if(followers.contains(follower)){
//            followers.remove(follower);
//        }else{
//            throw new IllegalArgumentException("User:"+follower.username+" already in followers of: "+ this.username);
//        }
//    }
}



