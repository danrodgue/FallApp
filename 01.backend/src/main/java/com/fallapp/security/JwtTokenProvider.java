package com.fallapp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Proveedor de tokens JWT para autenticación stateless.
 * 
 * <p>Esta clase maneja el ciclo completo de vida de los tokens JWT:
 * <ul>
 *   <li>Generación de tokens a partir de Authentication o username</li>
 *   <li>Validación de firma y expiración</li>
 *   <li>Extracción de claims (username, expiration)</li>
 * </ul>
 * 
 * <p><b>Configuración</b>:
 * <ul>
 *   <li>Algoritmo: HS512 (HMAC-SHA512)</li>
 *   <li>Secret mínimo: 512 bits (64 caracteres)</li>
 *   <li>Expiración: Configurable via application.properties</li>
 * </ul>
 * 
 * <p><b>Seguridad</b>:
 * - El secret se configura en application.properties (jwt.secret)
 * - En producción usar variables de entorno o secret manager
 * - Tokens firmados no pueden ser alterados sin detectar
 * - Validación de expiración en cada request
 * 
 * @see io.jsonwebtoken.Jwts
 * @see JwtAuthenticationFilter
 * @see UserDetailsServiceImpl
 * @see <a href="/srv/FallApp/04.docs/arquitectura/ADR-006-autenticacion-jwt-pendiente.md">ADR-006</a>
 * 
 * @author FallApp Team
 * @version 0.3.0
 * @since 2026-02-01
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 horas por defecto
    private long jwtExpiration;

    /**
     * Genera un token JWT para un usuario autenticado.
     *
     * @param authentication Objeto de autenticación de Spring Security
     * @return Token JWT firmado
     */
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Genera un token JWT directamente desde un nombre de usuario.
     * Útil para registro de nuevos usuarios.
     *
     * @param username Nombre de usuario
     * @return Token JWT firmado
     */
    public String generateTokenFromUsername(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extrae el nombre de usuario del token JWT.
     *
     * @param token Token JWT
     * @return Nombre de usuario
     */
    public String getUsernameFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Valida un token JWT.
     *
     * @param authToken Token a validar
     * @return true si el token es válido, false en caso contrario
     */
    public boolean validateToken(String authToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(authToken);
            
            return true;
        } catch (MalformedJwtException ex) {
            System.err.println("Token JWT malformado: " + ex.getMessage());
        } catch (ExpiredJwtException ex) {
            System.err.println("Token JWT expirado: " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            System.err.println("Token JWT no soportado: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string vacío: " + ex.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException ex) {
            System.err.println("Fallo en la firma JWT: " + ex.getMessage());
        }
        return false;
    }

    /**
     * Extrae la fecha de expiración del token.
     *
     * @param token Token JWT
     * @return Fecha de expiración
     */
    public Date getExpirationDateFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }
}
