package org.apostolis.security;

import io.javalin.http.ForbiddenResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apostolis.users.domain.Role;

import java.security.Key;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/* This class implements the Json Web Token authentication and authorization management. */

public class JjwtTokenManagerImpl implements TokenManager{
    private static final long EXPIRE_AFTER_MINS = 60;
    private final Key key;

    public JjwtTokenManagerImpl(){
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    @Override
    public String issueToken(String username, Role role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("Role",role)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(EXPIRE_AFTER_MINS)))
                .signWith(key)
                .compact();
    }
    @Override
    public boolean validateToken(String token) {
        try {
            Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration();
            return expiration.after(new Date());
        } catch (Exception ex){
            throw new ForbiddenResponse("Token has expired or is invalid");
        }
    }

    @Override
    public Role extractRole(String token){
        return Role.valueOf(Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("Role").toString());
    }

    @Override
    public String extractUsername(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }
}
