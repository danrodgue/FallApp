package com.fallapp.security;

import com.fallapp.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para JwtTokenProvider.
 * 
 * Valida:
 * - Generación de tokens JWT válidos
 * - Validación de firma y expiración
 * - Extracción correcta de claims (username, expiration)
 * - Manejo de tokens inválidos/expirados
 * 
 * @see JwtTokenProvider
 * @see <a href="/srv/FallApp/04.docs/arquitectura/ADR-006-autenticacion-jwt-pendiente.md">ADR-006</a>
 */
@DisplayName("JWT Token Provider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private String testSecret;
    private long testExpiration;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        
        // Secret de 64+ caracteres para HS512
        testSecret = "ClaveSecretaFallApp2026MuySeguraYLargaParaProduccionConMuchosMasCaracteres123!";
        testExpiration = 3600000L; // 1 hora para tests
        
        // Inyectar valores con ReflectionTestUtils
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", testExpiration);
    }

    @Test
    @DisplayName("Debe generar token JWT válido desde Authentication")
    void testGenerateTokenFromAuthentication() {
        // Given
        org.springframework.security.core.userdetails.User userDetails = 
            new org.springframework.security.core.userdetails.User(
                "test@fallapp.es",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // When
        String token = jwtTokenProvider.generateToken(authentication);

        // Then
        assertNotNull(token, "El token no debe ser null");
        assertTrue(token.length() > 100, "El token debe tener longitud suficiente");
        assertTrue(token.startsWith("eyJ"), "El token JWT debe empezar con 'eyJ'");
    }

    @Test
    @DisplayName("Debe generar token JWT válido desde username")
    void testGenerateTokenFromUsername() {
        // Given
        String username = "admin@fallapp.es";

        // When
        String token = jwtTokenProvider.generateTokenFromUsername(username);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 100);
        
        // Verificar que el token contiene el username correcto
        String extractedUsername = jwtTokenProvider.getUsernameFromToken(token);
        assertEquals(username, extractedUsername);
    }

    @Test
    @DisplayName("Debe validar token correcto como TRUE")
    void testValidateToken_Valid() {
        // Given
        String token = jwtTokenProvider.generateTokenFromUsername("test@fallapp.es");

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertTrue(isValid, "Un token recién generado debe ser válido");
    }

    @Test
    @DisplayName("Debe rechazar token con firma inválida")
    void testValidateToken_InvalidSignature() {
        // Given - Token generado con secret diferente
        SecretKey wrongKey = Keys.hmacShaKeyFor("OtroSecretoDiferenteMuyLargoParaHS512ConMuchosMasCaracteres456!".getBytes(StandardCharsets.UTF_8));
        String invalidToken = Jwts.builder()
                .subject("test@fallapp.es")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(wrongKey)
                .compact();

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertFalse(isValid, "Token con firma inválida debe ser rechazado");
    }

    @Test
    @DisplayName("Debe rechazar token expirado")
    void testValidateToken_Expired() {
        // Given - Token con expiración en el pasado
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("test@fallapp.es")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // Hace 2 horas
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // Expiró hace 1 hora
                .signWith(key)
                .compact();

        // When
        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        // Then
        assertFalse(isValid, "Token expirado debe ser rechazado");
    }

    @Test
    @DisplayName("Debe extraer username correctamente del token")
    void testGetUsernameFromToken() {
        // Given
        String expectedUsername = "demo@fallapp.es";
        String token = jwtTokenProvider.generateTokenFromUsername(expectedUsername);

        // When
        String actualUsername = jwtTokenProvider.getUsernameFromToken(token);

        // Then
        assertEquals(expectedUsername, actualUsername);
    }

    @Test
    @DisplayName("Debe extraer fecha de expiración del token")
    void testGetExpirationDateFromToken() {
        // Given
        String token = jwtTokenProvider.generateTokenFromUsername("test@fallapp.es");
        long now = System.currentTimeMillis();

        // When
        Date expirationDate = jwtTokenProvider.getExpirationDateFromToken(token);

        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.getTime() > now, "La expiración debe ser en el futuro");
        
        // Verificar que está aproximadamente en 1 hora (testExpiration)
        long diff = expirationDate.getTime() - now;
        assertTrue(diff > 3500000 && diff < 3700000, 
                "La expiración debe estar cerca de 1 hora (3600000ms)");
    }

    @Test
    @DisplayName("Debe rechazar token malformado")
    void testValidateToken_Malformed() {
        // Given
        String malformedToken = "esto.no.es.un.jwt.valido";

        // When
        boolean isValid = jwtTokenProvider.validateToken(malformedToken);

        // Then
        assertFalse(isValid, "Token malformado debe ser rechazado");
    }

    @Test
    @DisplayName("Debe rechazar token vacío")
    void testValidateToken_Empty() {
        // When
        boolean isValid = jwtTokenProvider.validateToken("");

        // Then
        assertFalse(isValid, "Token vacío debe ser rechazado");
    }

    @Test
    @DisplayName("Debe rechazar token null")
    void testValidateToken_Null() {
        // When
        boolean isValid = jwtTokenProvider.validateToken(null);

        // Then
        assertFalse(isValid, "Token null debe ser rechazado");
    }

    @Test
    @DisplayName("Token debe contener subject (username) en claims")
    void testTokenContainsSubject() {
        // Given
        String username = "casal@fallapp.es";
        String token = jwtTokenProvider.generateTokenFromUsername(username);
        
        // Parse token manually to check claims
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Then
        assertEquals(username, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("Tokens diferentes para usuarios diferentes")
    void testDifferentUsersGenerateDifferentTokens() {
        // Given
        String user1 = "user1@fallapp.es";
        String user2 = "user2@fallapp.es";

        // When
        String token1 = jwtTokenProvider.generateTokenFromUsername(user1);
        String token2 = jwtTokenProvider.generateTokenFromUsername(user2);

        // Then
        assertNotEquals(token1, token2, "Usuarios diferentes deben generar tokens diferentes");
        assertEquals(user1, jwtTokenProvider.getUsernameFromToken(token1));
        assertEquals(user2, jwtTokenProvider.getUsernameFromToken(token2));
    }

    @Test
    @DisplayName("Tokens generados en momentos diferentes son distintos")
    void testTokensGeneratedAtDifferentTimesAreDifferent() throws InterruptedException {
        // Given
        String username = "test@fallapp.es";
        
        // When
        String token1 = jwtTokenProvider.generateTokenFromUsername(username);
        Thread.sleep(1100); // Esperar 1.1s para diferencia en segundos (JWT iat es en segundos)
        String token2 = jwtTokenProvider.generateTokenFromUsername(username);

        // Then
        assertNotEquals(token1, token2, 
                "Tokens del mismo usuario generados en diferentes momentos deben ser distintos");
    }
}
