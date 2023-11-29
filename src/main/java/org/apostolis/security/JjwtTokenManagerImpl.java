package org.apostolis.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apostolis.AppConfig;
import org.apostolis.exception.InvalidTokenException;
import org.apostolis.users.domain.Role;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

// Json Web Token authentication and authorization management.

public class JjwtTokenManagerImpl implements TokenManager{
    private static final long EXPIRE_AFTER_MINS = 60;
    private final Key key;

    private final String SECRET = "sfdghtuhgruitjkkourijkldjlifgjdfuiryuytukhg";

    public JjwtTokenManagerImpl(){

        //this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        this.key = getSignInKey();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @Override
    public String issueToken(String username, Role role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("Role",role)
                .setIssuedAt(Date.from(Instant.now(AppConfig.getClock())))
                .setExpiration(Date.from(Instant.now(AppConfig.getClock()).plusSeconds(60*EXPIRE_AFTER_MINS)))
                .signWith(key)
                .compact();
    }
    @Override
    public boolean validateToken(String token) throws InvalidTokenException {
        try {
            Date expiration = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getExpiration();
            return expiration.after(new Date());
        } catch (Exception ex){
            throw new InvalidTokenException(ex);
        }
    }

    @Override
    public String extractRole(String token) throws InvalidTokenException {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().get("Role").toString();
        }catch (Exception ex){
            throw new InvalidTokenException(ex);
        }
    }

    @Override
    public String extractUsername(String token) throws InvalidTokenException {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
        }catch (Exception ex){
            throw new InvalidTokenException(ex);
        }

    }
}
