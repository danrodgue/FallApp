package com.fallapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fallapp.model.Comentario;
import com.fallapp.repository.ComentarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

/**
 * Servicio encargado de llamar a la IA de Hugging Face
 * para analizar el sentimiento de los comentarios.
 *
 * Diseño:
 * - Se ejecuta de forma ASÍNCRONA (@Async) para no bloquear la petición principal.
 * - Usa la Inference API del modelo:
 *   lxyuan/distilbert-base-multilingual-cased-sentiments-student
 * - Solo persiste la etiqueta principal (positive / neutral / negative) en BD.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SentimentAnalysisService {

    private final ComentarioRepository comentarioRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${huggingface.api.token:}")
    private String huggingFaceToken;

    private static final String MODEL_URL =
            "https://api-inference.huggingface.co/models/lxyuan/distilbert-base-multilingual-cased-sentiments-student";

    /**
     * Analiza el contenido de un comentario y actualiza su campo `sentimiento` en BD.
     * Se ejecuta en segundo plano.
     */
    @Async
    public void analizarComentarioAsync(Long comentarioId, String texto) {
        if (huggingFaceToken == null || huggingFaceToken.isBlank()) {
            log.warn("HuggingFace token no configurado. Saltando análisis de sentimiento.");
            return;
        }

        try {
            String label = llamarModeloYObtenerSentimiento(texto);
            if (label == null) {
                log.warn("No se pudo determinar sentimiento para comentario {}", comentarioId);
                return;
            }

            // Persistir resultado en la entidad Comentario
            Optional<Comentario> optionalComentario = comentarioRepository.findById(comentarioId);
            if (optionalComentario.isEmpty()) {
                log.warn("Comentario {} no encontrado al intentar guardar sentimiento", comentarioId);
                return;
            }

            Comentario comentario = optionalComentario.get();
            comentario.setSentimiento(label);
            comentarioRepository.save(comentario);

            log.info("Sentimiento '{}' guardado para comentario {}", label, comentarioId);
        } catch (Exception e) {
            log.error("Error analizando sentimiento para comentario {}: {}", comentarioId, e.getMessage());
        }
    }

    /**
     * Llama al modelo de Hugging Face y devuelve la etiqueta con mayor score.
     */
    private String llamarModeloYObtenerSentimiento(String texto) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(huggingFaceToken);

        Map<String, Object> body = new HashMap<>();
        body.put("inputs", texto);
        body.put("options", Map.of(
            "wait_for_model", true,
            "use_cache", false
        ));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(MODEL_URL, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.warn("Llamada a Hugging Face falló con status {}: {}",
                    response.getStatusCode(), response.getBody());
            return null;
        }

        String json = response.getBody();
        if (json == null || json.isBlank()) {
            return null;
        }

        JsonNode root = objectMapper.readTree(json);

        if (root.isObject() && root.has("error")) {
            log.warn("Hugging Face devolvió error: {}", root.path("error").asText());
            return null;
        }

        List<SentimentScore> scores = extraerScores(root);

        if (scores.isEmpty()) {
            log.warn("Formato inesperado de respuesta Hugging Face: {}", json);
            return null;
        }

        return scores.stream()
                .max(Comparator.comparingDouble(SentimentScore::getScore))
                .map(s -> normalizarEtiqueta(s.getLabel()))
                .orElse(null);
    }

    private List<SentimentScore> extraerScores(JsonNode node) throws Exception {
        List<SentimentScore> scores = new ArrayList<>();
        recolectarScores(node, scores);
        return scores;
    }

    private void recolectarScores(JsonNode node, List<SentimentScore> scores) throws Exception {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                recolectarScores(child, scores);
            }
            return;
        }

        if (node.isObject()) {
            if (node.has("label") && node.has("score")) {
                SentimentScore score = objectMapper.convertValue(node, SentimentScore.class);
                scores.add(score);
                return;
            }

            if (node.has("result")) {
                recolectarScores(node.get("result"), scores);
            }
            if (node.has("data")) {
                recolectarScores(node.get("data"), scores);
            }
        }
    }

    /**
     * Normaliza la etiqueta devuelta por el modelo a:
     * positive / neutral / negative (lowercase).
     */
    private String normalizarEtiqueta(String rawLabel) {
        if (rawLabel == null) return null;
        String label = rawLabel.toLowerCase();

        if (label.contains("5") || label.contains("4")) return "positive";
        if (label.contains("3")) return "neutral";
        if (label.contains("1") || label.contains("2")) return "negative";

        if (label.contains("pos")) return "positive";
        if (label.contains("neu")) return "neutral";
        if (label.contains("neg")) return "negative";

        if (label.contains("positivo")) return "positive";
        if (label.contains("neutro")) return "neutral";
        if (label.contains("negativo")) return "negative";

        return label;
    }

    /**
     * DTO interno para parsear resultados de Hugging Face.
     */
    public static class SentimentScore {
        private String label;
        private double score;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public double getScore() {
            return score;
        }

        public void setScore(double score) {
            this.score = score;
        }
    }
}

