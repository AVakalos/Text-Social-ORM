package org.apostolis.users.domain;

import lombok.Getter;
import org.apostolis.exception.AuthenticationException;
import org.apostolis.security.PasswordEncoder;
import org.apostolis.security.TokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

@Getter
public class User{
    private UserId id;
    private final String username;
    private final String password;
    private final String role;
    private final Set<User> following_users = new HashSet<>();;
    private final Set<User> followers = new HashSet<>();;

    private static final Logger logger = LoggerFactory.getLogger(User.class);

    public User(String username, String password, String role){
        this.username = username;
        this.password = password;
        this.role = role;
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
        }
        following_users.add(followingUser);
    }

    public void removeFollowingUser(User followingUser){
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



