package com.fallapp.service;

import com.fallapp.dto.VotoDTO;
import com.fallapp.dto.CrearVotoRequest;
import com.fallapp.model.Falla;
import com.fallapp.model.Falla;
import com.fallapp.model.Usuario;
import com.fallapp.model.Voto;
import com.fallapp.exception.BadRequestException;
import com.fallapp.exception.ResourceNotFoundException;
import com.fallapp.repository.NinotRepository;
import com.fallapp.repository.UsuarioRepository;
import com.fallapp.repository.VotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de votos
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VotoService {

        private final VotoRepository votoRepository;
        private final UsuarioRepository usuarioRepository;
        private final com.fallapp.repository.FallaRepository fallaRepository;

    /**
     * Crear un nuevo voto
     */
    public VotoDTO votar(Long idUsuario, CrearVotoRequest request) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));

        // Verificar que la falla existe y obtenerla
        Voto.TipoVoto tipo = Voto.TipoVoto.valueOf(request.getTipoVoto());
        Falla falla = fallaRepository.findById(request.getIdFalla())
                .orElseThrow(() -> new ResourceNotFoundException("Falla", "id", request.getIdFalla()));
        
        if (votoRepository.existsByUsuarioAndFallaAndTipoVoto(usuario, falla, tipo)) {
            throw new BadRequestException("Ya has votado esta falla con el tipo: " + request.getTipoVoto());
        }

        // Crear el voto
        Voto voto = new Voto();
        voto.setUsuario(usuario);
        voto.setFalla(falla);
        voto.setTipoVoto(tipo);
        // Normalizar valor a 1 para indicar presencia de voto
        voto.setValor(1);

        Voto guardado = votoRepository.save(voto);
        return convertirADTO(guardado);
    }

    /**
     * Obtener votos de un usuario
     */
    @Transactional(readOnly = true)
    public List<VotoDTO> obtenerVotosUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", idUsuario));
        
        return votoRepository.findByUsuario(usuario, Pageable.unpaged())
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener votos de una falla
     */
    @Transactional(readOnly = true)
    public List<VotoDTO> obtenerVotosFalla(Long idFalla) {
        Falla falla = fallaRepository.findById(idFalla)
                .orElseThrow(() -> new ResourceNotFoundException("Falla", "id", idFalla));

        return votoRepository.findByFalla(falla, Pageable.unpaged())
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Eliminar un voto
     */
    public void eliminar(Long idVoto, Long idUsuario) {
        Voto voto = votoRepository.findById(idVoto)
                .orElseThrow(() -> new ResourceNotFoundException("Voto", "id", idVoto));

        // Verificar que el voto pertenece al usuario
        if (!voto.getUsuario().getIdUsuario().equals(idUsuario)) {
            throw new BadRequestException("No puedes eliminar un voto que no es tuyo");
        }

        votoRepository.delete(voto);
    }

    /**
     * Convertir entidad a DTO
     */
    private VotoDTO convertirADTO(Voto voto) {
        return VotoDTO.builder()
                .idVoto(voto.getIdVoto())
                .idUsuario(voto.getUsuario().getIdUsuario())
                .nombreUsuario(voto.getUsuario().getNombreCompleto())
                .idFalla(voto.getFalla().getIdFalla())
                .nombreFalla(voto.getFalla().getNombre())
                .tipoVoto(voto.getTipoVoto().name())
                .fechaCreacion(voto.getCreadoEn())
                .build();
    }
}
