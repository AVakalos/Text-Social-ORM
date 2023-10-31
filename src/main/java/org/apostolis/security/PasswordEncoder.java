package org.apostolis.security;

import org.springframework.security.crypto.bcrypt.BCrypt;

/* This class is used for the password encryption and checking at signup and login operations. */

public class PasswordEncoder {
    public String encodePassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt(10));
    }
    public boolean checkPassword(String password, String hashedPassword){
        return BCrypt.checkpw(password, hashedPassword);
    }
}
