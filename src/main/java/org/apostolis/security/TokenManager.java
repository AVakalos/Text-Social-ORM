package org.apostolis.security;


import org.apostolis.exception.InvalidTokenException;
import org.apostolis.users.domain.Role;

public interface TokenManager {
    String issueToken (String username, Role role);
    boolean validateToken (String token) throws InvalidTokenException;
    Role extractRole(String token) throws InvalidTokenException;
    String extractUsername(String token) throws InvalidTokenException;
}
