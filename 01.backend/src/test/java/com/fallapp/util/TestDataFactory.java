package com.fallapp.util;

import com.fallapp.dto.*;
import com.fallapp.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Factory para crear datos de test consistentes
 * Patrón Builder para facilitar el debugging
 *
 * @version 1.0.0
 */
public class TestDataFactory {

    // ==========================================
    // USUARIOS
    // ==========================================

    public static Usuario crearUsuario() {
        return crearUsuario(1L, "test@example.com", "Test User", Usuario.RolUsuario.usuario);
    }

    public static Usuario crearUsuario(Long id, String email, String nombre, Usuario.RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setEmail(email);
        usuario.setNombreCompleto(nombre);
        usuario.setContrasenaHash("$2a$10$hashedPassword");
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.now());
        return usuario;
    }

    public static Usuario crearAdmin() {
        return crearUsuario(99L, "admin@fallapp.com", "Admin", Usuario.RolUsuario.admin);
    }

    public static Usuario crearCasal() {
        return crearUsuario(50L, "casal@falla.com", "Casal User", Usuario.RolUsuario.casal);
    }

    // ==========================================
    // FALLAS
    // ==========================================

    public static Falla crearFalla() {
        return crearFalla(1L, "Falla Na Jordana", "E");
    }

    public static Falla crearFalla(Long id, String nombre, String seccion) {
        Falla falla = new Falla();
        falla.setIdFalla(id);
        falla.setNombre(nombre);
        falla.setSeccion(seccion);
        falla.setPresidente("Presidente Test");
        falla.setArtista("Artista Test");
        falla.setAnyoFundacion(1950);
        falla.setExperim(false);
        falla.setUbicacionLat(BigDecimal.valueOf(39.4738));
        falla.setUbicacionLon(BigDecimal.valueOf(-0.3753));
        falla.setCategoria(Falla.CategoriaFalla.especial);
        falla.setCreadoEn(LocalDateTime.now());
        falla.setActualizadoEn(LocalDateTime.now());
        falla.setEventos(new ArrayList<>());
        falla.setUsuarios(new ArrayList<>());
        falla.setComentarios(new ArrayList<>());
        return falla;
    }

    // ==========================================
    // VOTOS
    // ==========================================

    public static Voto crearVoto(Long id, Usuario usuario, Falla falla, Voto.TipoVoto tipo) {
        Voto voto = new Voto();
        voto.setIdVoto(id);
        voto.setUsuario(usuario);
        voto.setFalla(falla);
        voto.setTipoVoto(tipo);
        voto.setValor(1);
        voto.setCreadoEn(LocalDateTime.now());
        return voto;
    }

    public static Voto crearVotoExperimental() {
        return crearVoto(1L, crearUsuario(), crearFalla(), Voto.TipoVoto.EXPERIMENTAL);
    }

    // ==========================================
    // EVENTOS
    // ==========================================

    public static Evento crearEvento() {
        return crearEvento(1L, crearFalla(), Evento.TipoEvento.planta, "Plantà 2026");
    }

    public static Evento crearEvento(Long id, Falla falla, Evento.TipoEvento tipo, String nombre) {
        Evento evento = new Evento();
        evento.setIdEvento(id);
        evento.setFalla(falla);
        evento.setTipo(tipo);
        evento.setNombre(nombre);
        evento.setDescripcion("Descripción del evento test");
        evento.setFechaEvento(LocalDateTime.of(2026, 3, 15, 0, 0));
        evento.setUbicacion("Plaza del Ayuntamiento");
        evento.setParticipantesEstimado(500);
        evento.setCreadoEn(LocalDateTime.now());
        return evento;
    }

    // ==========================================
    // COMENTARIOS
    // ==========================================

    public static Comentario crearComentario() {
        Comentario comentario = new Comentario();
        comentario.setIdComentario(1L);
        comentario.setContenido("Comentario de prueba");
        comentario.setUsuario(crearUsuario());
        comentario.setFalla(crearFalla());
        comentario.setCreadoEn(LocalDateTime.now());
        return comentario;
    }

    // ==========================================
    // DTOs
    // ==========================================

    public static CrearVotoRequest crearVotoRequest(Long idFalla, String tipoVoto) {
        CrearVotoRequest request = new CrearVotoRequest();
        request.setIdFalla(idFalla);
        request.setTipoVoto(tipoVoto);
        return request;
    }

    public static RegistroRequest crearRegistroRequest(String email, String password, String nombre) {
        RegistroRequest request = new RegistroRequest();
        request.setEmail(email);
        request.setContrasena(password);
        request.setNombreCompleto(nombre);
        return request;
    }

    public static FallaDTO crearFallaDTO(String nombre, String seccion, String presidente) {
        return FallaDTO.builder()
                .nombre(nombre)
                .seccion(seccion)
                .presidente(presidente)
                .anyoFundacion(2000)
                .categoria("primera")
                .build();
    }

    public static VotoDTO crearVotoDTO(Long idVoto, Long idUsuario, Long idFalla, String tipo) {
        return VotoDTO.builder()
                .idVoto(idVoto)
                .idUsuario(idUsuario)
                .nombreUsuario("Test User")
                .idFalla(idFalla)
                .nombreFalla("Test Falla")
                .tipoVoto(tipo)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}
