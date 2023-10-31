package org.apostolis.security;


import org.apostolis.users.domain.Role;

public interface TokenManager {
    String issueToken (String username, Role role);
    boolean validateToken (String token);
    Role extractRole(String token);
    String extractUsername(String token);
}
