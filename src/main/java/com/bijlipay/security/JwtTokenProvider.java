package com.bijlipay.security;

import com.bijlipay.model.dto.PasswordToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    @Value("${app.jwtSecret}")
    private String jwtSecret;
    @Value("${app.jwtExpirationInMs}")
    private int jwtExpirationInMs;

    public Date getExpirationDateFromToken(String token) {
        return (Date)this.getClaimsBody(token, Claims::getExpiration);
    }

    public int getUserIdFromJWT(String token) {
        return Integer.parseInt((String)this.getClaimsBody(token, Claims::getSubject));
    }

    private <T> T getClaimsBody(String token, Function<Claims, T> clamis) {
        Claims claimsbody = this.getAllClaimsFromToken(token);
        return (T)clamis.apply(claimsbody);
    }

    private Claims getAllClaimsFromToken(String token) {
        return (Claims)Jwts.parser().setSigningKey(this.jwtSecret).parseClaimsJws(token).getBody();
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal)authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + (long)this.jwtExpirationInMs);
        return Jwts.builder().setSubject(Long.toString(userPrincipal.getId())).setIssuedAt(new Date()).setExpiration(expiryDate).signWith(SignatureAlgorithm.HS512, this.jwtSecret).compact();
    }

    public String generatePasswordToken(PasswordToken token) {
        Claims claims = Jwts.claims().setSubject(String.valueOf(token.getUserId()));
        claims.put("userId", token.getUserId());
        claims.put("used", token.isUsed());
        claims.put("oneTimePassword", token.getOneTimePassword());
        return Jwts.builder().setClaims(claims).signWith(SignatureAlgorithm.HS512, this.jwtSecret).setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis() + 1800000000L)).compact();
    }

    public PasswordToken validatePasswordToken(String token) throws UnsupportedEncodingException {
        token = URLDecoder.decode(token, "UTF-8");

        try {
            Claims body = (Claims)Jwts.parser().setSigningKey(this.jwtSecret).parseClaimsJws(token).getBody();
            if (body.get("userId") != null && !body.get("userId").equals("") && body.getExpiration().getTime() > System.currentTimeMillis()) {
                PasswordToken passwordToken = new PasswordToken();
                passwordToken.setOneTimePassword((Integer)body.get("oneTimePassword"));
                passwordToken.setUserId((long)(Integer)body.get("userId"));
                passwordToken.setUsed((Boolean)body.get("used"));
                return passwordToken;
            } else {
                throw new RuntimeException("Invalid token!");
            }
        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(this.jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (SignatureException var3) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException var4) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException var5) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException var6) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException var7) {
            logger.error("JWT claims string is empty.");
        }

        return false;
    }
}