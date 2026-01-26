package com.example.Fallapp.controller;

import com.example.Fallapp.model.Falla;
import com.example.Fallapp.repository.FallaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/fallas")
public class FallaController {

    private final FallaRepository fallaRepository;

    public FallaController(FallaRepository fallaRepository) {
        this.fallaRepository = fallaRepository;
    }

    @GetMapping
    public List<Falla> obtenerTodas() {
        return fallaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Falla> obtenerPorId(@PathVariable String id) {
        Optional<Falla> falla = fallaRepository.findById(id);
        return falla.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Falla crear(@RequestBody Falla falla) {
        return fallaRepository.save(falla);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Falla> actualizar(@PathVariable String id, @RequestBody Falla falla) {
        if (!fallaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        falla.setId(id);
        Falla actualizada = fallaRepository.save(falla);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable String id) {
        if (!fallaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        fallaRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/seccion/{seccion}")
    public List<Falla> obtenerPorSeccion(@PathVariable String seccion) {
        return fallaRepository.findBySeccion(seccion);
    }


    @GetMapping("/buscar")
    public List<Falla> buscarPorLema(@RequestParam("lema") String lema) {
        return fallaRepository.findByLemaContainingIgnoreCase(lema);
    }

    @PutMapping("/{idFalla}/distintivo")
    public ResponseEntity<Void> actualizarDistintivo(
            @PathVariable Long idFalla,
            @RequestParam("nuevoDistintivo") String nuevoDistintivo) {

        fallaRepository.actualizarDistintivoPorIdFalla(idFalla, nuevoDistintivo);
        return ResponseEntity.noContent().build();
    }
}

