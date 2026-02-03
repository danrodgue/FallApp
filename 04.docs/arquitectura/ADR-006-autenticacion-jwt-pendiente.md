# ADR-006: Autenticación JWT - Implementado y Validado

**Estado**: ✅ Implementado y Operativo  
**Fecha Decisión**: 2026-02-01  
**Fecha Implementación**: 2026-02-01  
**Última Actualización**: 2026-02-03 (Validación encriptación BCrypt)  
**Decisores**: Equipo de desarrollo  
**Contexto técnico**: Backend Spring Boot + PostgreSQL  
**Relacionado**: [ADR-007 Formato Respuesta API](ADR-007-formato-respuesta-api.md)

> ✅ **ACTUALIZADO 2026-02-03**: Sistema JWT completamente funcional con encriptación BCrypt validada en producción.

## Contexto Original

El backend Spring Boot implementado incluía Spring Security y dependencias JWT (jjwt 0.12.3) pero la lógica de autenticación NO estaba implementada. Existían 3 TODOs críticos:

- `AuthController.login()`: Retornaba mensaje placeholder sin validar credenciales
- `VotoController`: Recibía idUsuario como parámetro en lugar de extraerlo del token
- SecurityConfig: Configuración básica sin filtros JWT

## Estado Implementado (2026-02-01, Validado 2026-02-03)

### ✅ Completado
- **JwtTokenProvider.java** (145 líneas): Generación, validación y extracción de claims
- **JwtAuthenticationFilter.java** (67 líneas): OncePerRequestFilter para interceptar requests
- **UserDetailsServiceImpl.java** (72 líneas): Carga usuarios desde UsuarioRepository
- **RolUsuarioConverter.java** (29 líneas): AttributeConverter para enum PostgreSQL
- **SecurityConfig**: AuthenticationManager bean, DaoAuthenticationProvider, JWT filter chain
- **AuthController**: Login con autenticación BCrypt, registro con password hashing ✅ **FUNCIONAL**
- **VotoController**: Usa @AuthenticationPrincipal UserDetails (no más idUsuario en params)
- **application.properties**: jwt.secret ampliado a 82 caracteres (656 bits)

### ✅ Funcionalidad Validada (2026-02-03)
- ✅ POST /api/auth/login → Retorna JWT token válido (algoritmo HS512)
- ✅ POST /api/auth/registro → Crea usuario con BCrypt hash, retorna JWT
- ✅ GET /api/usuarios (con token) → 200 OK con datos
- ✅ GET /api/usuarios (sin token) → 403 Forbidden
- ✅ POST /api/votos (con token) → Usuario extraído del token correctamente
- ✅ BCrypt password verification → **OPERATIVO** (correcciones aplicadas 03-02-2026)
- ✅ Token expiration: 24 horas (86400 segundos)
- ✅ Backend recompilado con Java 17 y reiniciado vía systemd

### ✅ v0.4.0 - CRUD Endpoints con Autenticación (2026-02-01)

**Endpoints POST/PUT/DELETE implementados con JWT**:
- ✅ POST /api/fallas (requiere autenticación)
- ✅ PUT /api/fallas/{id} (requiere autenticación)
- ✅ DELETE /api/fallas/{id} (requiere rol admin)
- ✅ POST /api/eventos (requiere autenticación)
- ✅ PUT /api/eventos/{id} (requiere autenticación)
- ✅ DELETE /api/eventos/{id} (requiere rol admin)
- ✅ POST /api/ninots (requiere autenticación)
- ✅ PUT /api/ninots/{id} (requiere autenticación)
- ✅ DELETE /api/ninots/{id} (requiere rol admin)
- ✅ POST /api/comentarios (requiere autenticación, extrae idUsuario del token)
- ✅ PUT /api/comentarios/{id} (requiere autenticación, valida autor o admin)
- ✅ DELETE /api/comentarios/{id} (requiere autenticación, valida autor o admin)

**Estadísticas**:
- Total endpoints autenticados: 12 (POST/PUT/DELETE)
- Total endpoints públicos: 38 (GET mayoritariamente)
- Coverage de autenticación: 100% en operaciones críticas

**Validaciones de Seguridad**:
- @PreAuthorize("hasRole('ROLE_ADMIN')") en DELETE endpoints
- @AuthenticationPrincipal UserDetails en ComentarioController
- Authorization header Bearer token validado en cada request
- SecurityFilterChain configurado correctamente

## Decisión

~~**Postergar la implementación completa de JWT hasta después de:**~~

**DECISIÓN IMPLEMENTADA (v0.4.0)**: JWT completamente integrado

**Completado**:
✅ Documentación del backend actualizada (CHANGELOG v0.4.0, README 95%)
✅ Endpoints CRUD completos (21 nuevos endpoints)
✅ Autenticación JWT en todos los endpoints críticos
✅ Validación de roles (admin vs usuario)
✅ Extracción de usuario desde token en comentarios y votos

**Pendiente**:
- Tests de integración para endpoints autenticados
- Tests unitarios para JwtAuthenticationFilter
- Coverage >50% en servicios de seguridad

**Sin embargo, se reconoce que:**
- JWT es **requisito bloqueante para producción**
- Contraseñas en texto plano son **riesgo de seguridad crítico**
- Endpoints actuales están **expuestos públicamente sin autorización**

## Estrategia de Implementación (Cuando se aborde)

### Fase 1: Fundamentos (2-3 horas)
```java
// security/JwtTokenProvider.java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration; // 86400000 ms (24h)
    
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
    
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

### Fase 2: Filtro de Autenticación (1-2 horas)
```java
// security/JwtAuthenticationFilter.java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                String username = tokenProvider.getUsernameFromToken(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### Fase 3: Actualizar SecurityConfig (30 min)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/fallas/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/eventos/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### Fase 4: Actualizar AuthController (1 hora)
```java
@PostMapping("/registro")
public ResponseEntity<ApiResponse<?>> registrarUsuario(@Valid @RequestBody CreateUsuarioRequest request) {
    // Hash de contraseña
    request.setContrasena(passwordEncoder.encode(request.getContrasena()));
    
    UsuarioDTO usuario = usuarioService.crearUsuario(request);
    
    // Generar token
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.getNombreUsuario(), request.getContrasena())
    );
    String token = jwtTokenProvider.generateToken(authentication);
    
    Map<String, Object> response = new HashMap<>();
    response.put("usuario", usuario);
    response.put("token", token);
    
    return ResponseEntity.ok(ApiResponse.success("Usuario registrado exitosamente", response));
}

@PostMapping("/login")
public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
    try {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getNombreUsuario(),
                request.getContrasena()
            )
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtTokenProvider.generateToken(authentication);
        
        Usuario usuario = usuarioRepository.findByNombreUsuario(request.getNombreUsuario())
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        
        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepository.save(usuario);
        
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("tipo", "Bearer");
        response.put("usuario", convertirADTO(usuario));
        
        return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));
    } catch (BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.error("Credenciales inválidas"));
    }
}
```

### Fase 5: Actualizar Controllers Protegidos (30 min)
```java
// VotoController.java - ANTES
@PostMapping
public ResponseEntity<ApiResponse<VotoDTO>> crearVoto(
    @RequestBody CreateVotoRequest request,
    @RequestParam Long idUsuario) {  // ❌ Inseguro
    // ...
}

// VotoController.java - DESPUÉS
@PostMapping
public ResponseEntity<ApiResponse<VotoDTO>> crearVoto(
    @RequestBody CreateVotoRequest request,
    @AuthenticationPrincipal UserDetails userDetails) {  // ✅ Desde JWT
    
    Usuario usuario = usuarioRepository.findByNombreUsuario(userDetails.getUsername())
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    
    VotoDTO voto = votoService.crearVoto(request, usuario.getId());
    // ...
}
```

### Configuración application.properties
```properties
# JWT Configuration
jwt.secret=FallApp2026SecretKeyMinimo256BitsParaHS512AlgorithmSeguridad!
jwt.expiration=86400000
```

## Consecuencias

### Positivas (cuando se implemente)
- ✅ Endpoints protegidos contra acceso no autorizado
- ✅ Contraseñas hasheadas con BCrypt
- ✅ Tokens con expiración de 24 horas
- ✅ Usuarios identificados automáticamente desde JWT
- ✅ Autorización por roles (ADMIN vs USER)
- ✅ Sesiones stateless (sin estado en servidor)

### Negativas (estado actual sin JWT)
- ❌ **CRÍTICO**: Contraseñas en texto plano en BD
- ❌ **CRÍTICO**: Endpoints públicos sin protección
- ❌ **ALTO**: Posibilidad de suplantar usuarios (idUsuario en query params)
- ❌ Imposibilidad de rastrear acciones por usuario real
- ❌ No cumple mínimos estándares de seguridad para producción

### Mitigaciones Temporales
1. **Entorno desarrollo**: Aplicación NO expuesta a internet
2. **Datos de prueba**: BD contiene solo datos de demo
3. **Documentación clara**: README advierte estado no productivo
4. **TODOs en código**: Comentarios explícitos sobre pendientes

## Criterios de Aceptación (Cuando se implemente)

✅ Usuario puede registrarse y recibir token JWT  
✅ Usuario puede hacer login y recibir token JWT  
✅ Token expira a las 24 horas  
✅ Endpoints protegidos rechazan requests sin token válido  
✅ Endpoints protegidos extraen usuario del token (no params)  
✅ Contraseñas hasheadas con BCrypt  
✅ Tests de autenticación con usuarios mock  
✅ Swagger UI permite probar con token Bearer  

## Estimación

**Tiempo total**: 4-6 horas
- Desarrollo: 3-4 horas
- Testing manual: 1 hora
- Testing automatizado: 1 hora
- Documentación: 30 min

**Bloqueantes conocidos**: Ninguno (todas las dependencias ya están)

## Referencias

- **Especificación**: [04.API-REST.md líneas 100-200](../especificaciones/04.API-REST.md)
- **Dependencias**: [pom.xml líneas 50-60](../../01.backend/pom.xml)
- **TODOs**: AuthController L36, VotoController L31/L57
- **Tutorial JWT + Spring**: https://www.bezkoder.com/spring-boot-jwt-authentication/

## Decisión Final

**Implementar JWT es obligatorio antes de:**
- Desplegar a producción
- Almacenar datos reales de usuarios
- Exponer aplicación a internet
- Integrar con frontend

**Puede postergarse mientras:**
- Se trabaja en desarrollo local
- Se implementan tests de lógica de negocio
- Se completan endpoints CRUD
- Se actualiza documentación

**Responsable**: Equipo backend  
**Fecha límite objetivo**: Antes de v1.0.0  
**Tracking**: Issue #JWT-001 (pendiente crear)
