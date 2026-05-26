package com.bijlipay.security;

import com.bijlipay.model.dto.PasswordToken;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    @Value("${app.jwtSecret}")
    private String jwtSecret;
    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    public Date getExpirationDateFromToken(String token) {
        return getClaimsBody(token, Claims::getExpiration);
    }

    public int getUserIdFromJWT(String token) {
        return Integer.parseInt(getClaimsBody(token, Claims::getSubject));
    }

    private <T> T getClaimsBody(String token, Function<Claims, T> clamis) {
        final Claims claimsbody = getAllClaimsFromToken(token);
        return clamis.apply(claimsbody);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);
        return Jwts.builder().setSubject(Long.toString(userPrincipal.getId())).setIssuedAt(new Date()).setExpiration(expiryDate).signWith(SignatureAlgorithm.HS512, jwtSecret).compact();
    }

    public String generatePasswordToken(PasswordToken token) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(token.getUserId()));
        claims.put("userId", token.getUserId());
        claims.put("used", token.isUsed());
        claims.put("oneTimePassword", token.getOneTimePassword());
        return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, jwtSecret).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis() + 1000000 * 60 * 30)).compact();
    }

    public PasswordToken validatePasswordToken(String token) throws UnsupportedEncodingException {
        token = URLDecoder.decode(token, "UTF-8");
        PasswordToken passwordToken;
        try {
            Claims body = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
            if (body.get("userId") != null && !body.get("userId").equals("") && body.getExpiration().getTime() > System.currentTimeMillis()) {
                passwordToken = new PasswordToken();
                passwordToken.setOneTimePassword((Integer) body.get("oneTimePassword"));
                passwordToken.setUserId((Integer) body.get("userId"));
                passwordToken.setUsed((Boolean) body.get("used"));
            } else {
                throw new RuntimeException("Invalid token!");
            }
        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
        return passwordToken;
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }
}
