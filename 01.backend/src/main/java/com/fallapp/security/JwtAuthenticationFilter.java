package com.fallapp.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT que intercepta todas las peticiones HTTP.
 * 
 * <p>Este filtro se ejecuta una vez por request y realiza:
 * <ol>
 *   <li>Extracción del token JWT del header Authorization</li>
 *   <li>Validación de firma y expiración del token</li>
 *   <li>Carga de UserDetails desde la base de datos</li>
 *   <li>Configuración de Authentication en SecurityContext</li>
 * </ol>
 * 
 * <p><b>Flujo de Procesamiento</b>:
 * <pre>
 * Request → Extraer token → Validar → Cargar usuario → Set Authentication → Controller
 * </pre>
 * 
 * <p><b>Header esperado</b>:
 * <pre>
 * Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
 * </pre>
 * 
 * <p><b>Manejo de Errores</b>:
 * - Token inválido/expirado: Log warning, continúa sin autenticación (retorna 403)
 * - Token ausente: Continúa sin autenticación (público o retorna 403)
 * - Excepciones: Log error, continúa filter chain
 * 
 * @see JwtTokenProvider
 * @see UserDetailsServiceImpl
 * @see org.springframework.web.filter.OncePerRequestFilter
 * @see <a href="/srv/FallApp/04.docs/arquitectura/ADR-006-autenticacion-jwt-pendiente.md">ADR-006</a>
 * 
 * @author FallApp Team
 * @version 0.3.0
 * @since 2026-02-01
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * Procesa cada petición HTTP para validar el token JWT.
     *
     * @param request Petición HTTP entrante
     * @param response Respuesta HTTP saliente
     * @param filterChain Cadena de filtros
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extraer token JWT del header
            String jwt = getJwtFromRequest(request);

            // Validar y procesar token
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);

                // Cargar detalles del usuario
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Crear objeto de autenticación
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("No se pudo establecer la autenticación del usuario en el contexto de seguridad", ex);
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el token JWT del header Authorization.
     * Formato esperado: "Bearer <token>"
     *
     * @param request Petición HTTP
     * @return Token JWT sin el prefijo "Bearer ", o null si no existe
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Eliminar "Bearer "
        }

        return null;
    }
}
