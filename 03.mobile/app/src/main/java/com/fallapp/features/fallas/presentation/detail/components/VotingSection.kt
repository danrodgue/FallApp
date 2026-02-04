package com.fallapp.features.fallas.presentation.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fallapp.features.fallas.domain.model.TipoVoto
import com.fallapp.features.fallas.presentation.detail.FallaDetailUiState

/**
 * Sección de votación para fallas.
 * 
 * Muestra botones para los 3 tipos de voto (INGENIOSO, CRITICO, ARTISTICO)
 * con indicadores visuales de votos existentes y estadísticas.
 * 
 * @param uiState Estado de la UI con información de votos
 * @param onVoteClick Callback cuando se presiona un botón de voto
 * @param onRemoveVote Callback para eliminar un voto existente
 * @param modifier Modifier de Compose
 */
@Composable
fun VotingSection(
    uiState: FallaDetailUiState,
    onVoteClick: (TipoVoto, Long) -> Unit,
    onRemoveVote: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "🗳️ Vota esta Falla",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Puedes votar en hasta 3 categorías diferentes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
            
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Botones de voto
            TipoVoto.entries.forEach { tipoVoto ->
                VoteButton(
                    tipoVoto = tipoVoto,
                    hasVoted = uiState.hasVotedWith(tipoVoto),
                    voteCount = uiState.countVotosBy(tipoVoto),
                    isLoading = uiState.isVoting,
                    onVoteClick = {
                        uiState.falla?.let { falla ->
                            // Temporalmente usamos idFalla como idNinot
                            // En producción, deberías obtener el primer ninot de la falla
                            onVoteClick(tipoVoto, falla.idFalla)
                        }
                    },
                    onRemoveVote = {
                        uiState.getVotoFor(tipoVoto)?.let { voto ->
                            onRemoveVote(voto.idVoto)
                        }
                    }
                )
            }
            
            // Mensaje de error
            uiState.voteError?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Estadísticas totales
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total de votos",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${uiState.votos.size}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Botón individual de voto.
 */
@Composable
private fun VoteButton(
    tipoVoto: TipoVoto,
    hasVoted: Boolean,
    voteCount: Int,
    isLoading: Boolean,
    onVoteClick: () -> Unit,
    onRemoveVote: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (hasVoted) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Info del voto
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = tipoVoto.getDisplayName(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (hasVoted) 
                            MaterialTheme.colorScheme.onPrimaryContainer 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    
                    if (hasVoted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Votado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                
                Text(
                    text = tipoVoto.getDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasVoted)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Contador de votos
                Text(
                    text = "$voteCount votos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            // Botón de acción
            if (hasVoted) {
                FilledTonalButton(
                    onClick = onRemoveVote,
                    enabled = !isLoading,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Quitar")
                }
            } else {
                Button(
                    onClick = onVoteClick,
                    enabled = !isLoading
                ) {
                    Text("Votar")
                }
            }
        }
    }
}

/**
 * Diálogo de confirmación de voto.
 */
@Composable
fun VoteConfirmationDialog(
    tipoVoto: TipoVoto,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = tipoVoto.getDisplayName().take(2),
                style = MaterialTheme.typography.headlineLarge
            )
        },
        title = {
            Text(
                text = "Confirmar Voto",
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "¿Quieres votar esta falla como:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = tipoVoto.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = tipoVoto.getDescription(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isLoading) "Votando..." else "Confirmar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}
