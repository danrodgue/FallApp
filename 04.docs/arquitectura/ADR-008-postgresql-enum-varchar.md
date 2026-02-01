# ADR-008: PostgreSQL ENUM vs VARCHAR para Enumeraciones

**Estado**: ⚠️ Workaround Temporal  
**Fecha**: 2026-02-01  
**Decisores**: Equipo de desarrollo  
**Contexto técnico**: PostgreSQL 13 + Hibernate/JPA  
**Relacionado**: [ADR-006 Autenticación JWT](ADR-006-autenticacion-jwt-pendiente.md)

## Contexto

Durante la implementación de JWT, se encontró un problema de incompatibilidad entre:
- **PostgreSQL**: Tipo ENUM custom `rol_usuario` (valores: admin, casal, usuario)
- **Hibernate/JPA**: Anotación `@Enumerated(EnumType.STRING)` envía VARCHAR
- **Resultado**: Error en INSERT/UPDATE: "column 'rol' is of type rol_usuario but expression is of type character varying"

### Problema Específico

```sql
-- Schema PostgreSQL
CREATE TYPE rol_usuario AS ENUM ('admin', 'casal', 'usuario');
CREATE TABLE usuarios (
    rol rol_usuario NOT NULL DEFAULT 'usuario'::rol_usuario
);
```

```java
// Model Java
@Enumerated(EnumType.STRING)
@Column(name = "rol", columnDefinition = "rol_usuario")
private RolUsuario rol = RolUsuario.usuario;
```

**Error en UPDATE**:
```
ERROR: column "rol" is of type rol_usuario but expression is of type character varying
Hint: You will need to rewrite or cast the expression.
```

## Opciones Consideradas

### Opción 1: Mantener ENUM + Custom Hibernate Type ❌
**Ventajas**:
- Validación a nivel de base de datos
- Integridad referencial estricta
- Menor espacio de almacenamiento

**Desventajas**:
- Requiere dependencia `hibernate-types` o implementación custom
- Mayor complejidad de configuración
- Dificulta migraciones (agregar nuevos roles requiere ALTER TYPE)
- Bloqueado por vistas que referencian la columna

**Intentos realizados**:
1. `@Convert(converter = RolUsuarioConverter.class)` → Sigue enviando VARCHAR
2. `ALTER TABLE usuarios ALTER COLUMN rol TYPE VARCHAR(20)` → ERROR: vista `v_actividad_usuarios` depende de la columna
3. `DROP VIEW + ALTER + CREATE VIEW` → ENUM persiste por default `'usuario'::rol_usuario`
4. `ALTER COLUMN rol DROP DEFAULT; ALTER TYPE; SET DEFAULT` → No aplica sin usar USING

### Opción 2: Migrar a VARCHAR + Constraint Check ✅ (Decisión Futura)
**Ventajas**:
- Compatible nativamente con JPA `@Enumerated(EnumType.STRING)`
- No requiere dependencias adicionales
- Facilita agregar nuevos valores (solo actualizar constraint)
- Simplifica desarrollo y debugging

**Desventajas**:
- Validación en constraint CHECK vs tipo nativo
- Ligero incremento en espacio (VARCHAR vs ENUM)
- Requiere migración coordinada (BD + vistas + triggers)

**Implementación propuesta**:
```sql
-- Migración futura
DROP VIEW v_actividad_usuarios CASCADE;
ALTER TABLE usuarios ALTER COLUMN rol DROP DEFAULT;
ALTER TABLE usuarios ALTER COLUMN rol TYPE VARCHAR(20) USING rol::text;
ALTER TABLE usuarios ALTER COLUMN rol SET DEFAULT 'usuario';
ALTER TABLE usuarios ADD CONSTRAINT check_rol_values 
    CHECK (rol IN ('admin', 'casal', 'usuario'));
-- Recrear vistas afectadas
```

### Opción 3: Workaround Temporal (Implementado) ⚠️
**Implementación actual**:
```java
// AuthController.java
// TODO: Actualizar último acceso (comentado temporalmente por issue ENUM rol_usuario)
// usuario.setUltimoAcceso(LocalDateTime.now());
// usuarioRepository.save(usuario);
```

**Impacto**:
- Login funciona sin errores ✅
- Último acceso NO se actualiza ❌
- Autenticación JWT completamente operativa ✅
- Endpoint POST /api/votos funcional ✅

## Decisión

**FASE 1 (Actual)**: Workaround temporal - Comentar UPDATE de `ultimo_acceso` en login
- Permite desarrollo inmediato sin bloquear JWT
- Autenticación funciona correctamente
- Campo `ultimo_acceso` se mantiene NULL (impacto menor)

**FASE 2 (Próxima)**: Migrar ENUM → VARCHAR con constraint CHECK
- Script de migración en `/srv/FallApp/07.datos/scripts/99.migracion.enum.to.varchar.sql`
- Ejecutar después de validar JWT en todos los endpoints
- Coordinar con equipo antes de aplicar en producción

## Consecuencias

### Positivas
- ✅ JWT implementado sin retrasos
- ✅ Solución simple y clara para el problema
- ✅ Plan de migración definido
- ✅ No afecta funcionalidad crítica

### Negativas
- ⚠️ Campo `ultimo_acceso` no se actualiza (métrica perdida temporalmente)
- ⚠️ Workaround debe revertirse en FASE 2
- ⚠️ Requiere migración coordinada de BD

### Riesgos
- **BAJO**: Pérdida temporal de métrica `ultimo_acceso` (no crítica)
- **MEDIO**: Olvidar migración FASE 2 (documentado en TODO + ADR)
- **BAJO**: Conflictos en merge si otros desarrollan sobre usuarios

## Issues Relacionados

### Columnas Similares con Problema
- `usuarios.rol` (ENUM rol_usuario) → **Bloqueado, workaround activo**
- `ninots.año_construccion` vs `anyo_construccion` → **Pendiente revisión**
- Otros ENUMs: `tipo_voto`, `tipo_evento`, `categoria_premio` → **Sin issues reportados aún**

### Seguimiento
- [ ] Validar JWT en todos los endpoints CRUD
- [ ] Crear script de migración `99.migracion.enum.to.varchar.sql`
- [ ] Ejecutar migración en entorno de desarrollo
- [ ] Probar actualización de `ultimo_acceso` post-migración
- [ ] Descomentar código en AuthController.java
- [ ] Actualizar este ADR a estado "Resuelto"

## Referencias

- **Error PostgreSQL**: https://stackoverflow.com/questions/10923213/
- **Hibernate Types Library**: https://github.com/vladmihalcea/hibernate-types
- **JPA AttributeConverter**: https://docs.oracle.com/javaee/7/api/javax/persistence/AttributeConverter.html
- **Spring Security Issue #9392**: Custom UserDetails with Enums

## Anexo: Código Afectado

### AuthController.java
```java
// LÍNEA 88-91 (comentado temporalmente)
// TODO: Actualizar último acceso (comentado temporalmente por issue ENUM rol_usuario)
// usuario.setUltimoAcceso(LocalDateTime.now());
// usuarioRepository.save(usuario);
```

### RolUsuarioConverter.java
```java
@Converter(autoApply = true)
public class RolUsuarioConverter implements AttributeConverter<RolUsuario, String> {
    @Override
    public String convertToDatabaseColumn(RolUsuario attribute) {
        return attribute == null ? null : attribute.name();
    }
    
    @Override
    public RolUsuario convertToEntityAttribute(String dbData) {
        return dbData == null ? null : RolUsuario.valueOf(dbData);
    }
}
```

**Nota**: Converter creado pero inefectivo hasta migración VARCHAR.

---

**Última actualización**: 2026-02-01  
**Responsable**: Backend Team  
**Review**: Pendiente tras migración FASE 2
