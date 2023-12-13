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
    private final Set<UserId> following_users;
    private final Set<UserId> followers;


    private static final Logger logger = LoggerFactory.getLogger(User.class);

    public User(String username, String password, String role){
        this.username = username;
        this.password = password;
        this.role = role;
        this.following_users = new HashSet<>();
        this.followers = new HashSet<>();
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
//
//    public void addFollowingUser(UserId id){
//        following_users.add(id);
//    }
//
//    public void removeFollowingUser(UserId id){
//        following_users.remove(id);
//    }
//
//    public void addFollower(UserId id){
//        followers.add(id);
//    }
//
//    public void removeFollower(UserId id){
//        followers.remove(id);
//    }
}



