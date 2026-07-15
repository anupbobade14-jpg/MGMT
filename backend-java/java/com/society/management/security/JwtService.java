package com.society.management.security;

import com.society.management.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final AppProperties props;

    public JwtService(AppProperties props) {
        this.props = props;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.getJwt().getSecret()));
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        long exp = props.getJwt().getExpirationMs();
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuer(props.getJwt().getIssuer())
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + exp))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String subject) {
        long exp = props.getJwt().getRefreshExpirationMs();
        Date now = new Date();
        return Jwts.builder()
                .subject(subject)
                .issuer(props.getJwt().getIssuer())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public String getSubject(String token) { return parse(token).getSubject(); }
    public boolean isExpired(String token) { return parse(token).getExpiration().before(new Date()); }
}
