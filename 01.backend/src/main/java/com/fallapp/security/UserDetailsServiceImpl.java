package com.fallapp.security;

import com.fallapp.exception.ResourceNotFoundException;
import com.fallapp.model.Usuario;
import com.fallapp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de UserDetailsService para Spring Security.
 * 
 * <p>Integra la autenticación de Spring Security con la base de datos
 * de usuarios de FallApp, cargando usuarios por email y mapeando roles.
 * 
 * <p><b>Funcionalidad</b>:
 * <ul>
 *   <li>Carga usuarios desde PostgreSQL via UsuarioRepository</li>
 *   <li>Mapea Usuario entity a Spring Security UserDetails</li>
 *   <li>Convierte roles a GrantedAuthority con prefijo "ROLE_"</li>
 *   <li>Verifica estado activo del usuario</li>
 * </ul>
 * 
 * <p><b>Mapeo de campos</b>:
 * <pre>
 * Usuario.email          → UserDetails.username
 * Usuario.contraseñaHash → UserDetails.password
 * Usuario.rol            → GrantedAuthority "ROLE_{rol}"
 * Usuario.activo         → UserDetails.enabled
 * </pre>
 * 
 * <p><b>Seguridad</b>:
 * - Email como identificador único (no nombreUsuario)
 * - Contraseñas hasheadas con BCrypt
 * - Usuarios inactivos marcados como disabled
 * - Roles con prefijo ROLE_ para Spring Security
 * 
 * @see org.springframework.security.core.userdetails.UserDetailsService
 * @see Usuario
 * @see UsuarioRepository
 * @see <a href="/srv/FallApp/04.docs/arquitectura/ADR-006-autenticacion-jwt-pendiente.md">ADR-006</a>
 * 
 * @author FallApp Team
 * @version 0.3.0
 * @since 2026-02-01
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Carga un usuario por su nombre de usuario.
     * Método requerido por Spring Security.
     *
     * @param username Nombre de usuario
     * @return UserDetails con la información del usuario
     * @throws UsernameNotFoundException si el usuario no existe
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username es el email del usuario
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Verificar que el usuario esté activo
        if (!usuario.getActivo()) {
            throw new UsernameNotFoundException("Usuario inactivo: " + username);
        }

        // Construir autoridades desde el rol del usuario
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));

        // Retornar UserDetails de Spring Security
        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getContrasenaHash())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!usuario.getActivo())
                .build();
    }

    /**
     * Carga un usuario por su ID.
     * Útil para obtener detalles del usuario autenticado.
     *
     * @param id ID del usuario
     * @return Usuario
     * @throws ResourceNotFoundException si el usuario no existe
     */
    @Transactional(readOnly = true)
    public Usuario loadUserById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }
}
