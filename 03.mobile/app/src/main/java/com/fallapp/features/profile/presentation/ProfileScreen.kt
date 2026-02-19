package com.fallapp.features.profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fallapp.core.config.ApiConfig
import com.fallapp.features.profile.domain.model.UsuarioPerfil
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.io.ByteArrayOutputStream
import java.time.format.DateTimeFormatter

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsState()
    val photoUploadMessage by viewModel.photoUploadMessage.collectAsState()
    val profileImageVersion by viewModel.profileImageVersion.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(photoUploadMessage) {
        photoUploadMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearPhotoUploadMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            when (uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is ProfileUiState.Success -> {
                    val usuario = (uiState as ProfileUiState.Success).usuario
                    ProfileContent(
                        usuario = usuario,
                        profileImageVersion = profileImageVersion,
                        isUploadingPhoto = isUploadingPhoto,
                        onUploadPhoto = { bytes, fileName, mimeType ->
                            viewModel.uploadProfileImage(bytes, fileName, mimeType)
                        },
                        onLogout = onLogout,
                        onSave = { nombreCompleto, telefono, direccion, ciudad, codigoPostal ->
                            viewModel.updateProfile(nombreCompleto, telefono, direccion, ciudad, codigoPostal)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is ProfileUiState.Error -> {
                    val mensaje = (uiState as ProfileUiState.Error).mensaje
                    ErrorContent(
                        mensaje = mensaje,
                        onRefresh = { viewModel.refreshProfile() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    usuario: UsuarioPerfil,
    profileImageVersion: Long,
    isUploadingPhoto: Boolean,
    onUploadPhoto: (ByteArray, String, String?) -> Unit,
    onLogout: () -> Unit,
    onSave: (String, String?, String?, String?, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditMode by remember { mutableStateOf(false) }
    var nombreCompleto by remember { mutableStateOf(usuario.nombreCompleto) }
    var telefono by remember { mutableStateOf(usuario.telefono ?: "") }
    var direccion by remember { mutableStateOf(usuario.direccion ?: "") }
    var ciudad by remember { mutableStateOf(usuario.ciudad ?: "") }
    var codigoPostal by remember { mutableStateOf(usuario.codigoPostal ?: "") }
    val profileImageUrl = "${ApiConfig.BASE_URL}/api/usuarios/${usuario.idUsuario}/imagen?t=$profileImageVersion"
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val payload = extractImagePayload(context.contentResolver, uri)
                if (payload != null) {
                    onUploadPhoto(payload.bytes, payload.fileName, payload.mimeType)
                }
            }
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .background(color = MaterialTheme.colorScheme.background)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header con Avatar y Título
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Avatar",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(64.dp)
                )

                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.Transparent, CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

            if (isEditMode) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    enabled = !isUploadingPhoto,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isUploadingPhoto) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Subiendo...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Subir foto")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Perfil de Usuario",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                if (!isEditMode) {
                    IconButton(
                        onClick = { isEditMode = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Tarjeta: Información Personal
        InfoCard(title = "Información Personal") {
            if (isEditMode) {
                EditableField(label = "Email", value = usuario.email, enabled = false)
                EditableField(label = "Nombre Completo", value = nombreCompleto, onValueChange = { nombreCompleto = it })
            } else {
                InfoField(label = "Email", value = usuario.email)
                InfoField(label = "Nombre Completo", value = usuario.nombreCompleto)
            }
        }

        // Tarjeta: Datos de Contacto
        InfoCard(title = "Datos de Contacto") {
            if (isEditMode) {
                EditableField(label = "Teléfono", value = telefono, onValueChange = { telefono = it })
                EditableField(label = "Dirección", value = direccion, onValueChange = { direccion = it })
                EditableField(label = "Ciudad", value = ciudad, onValueChange = { ciudad = it })
                EditableField(label = "Código Postal", value = codigoPostal, onValueChange = { codigoPostal = it })
            } else {
                InfoField(label = "Teléfono", value = usuario.telefono ?: "(vacío)")
                InfoField(label = "Dirección", value = usuario.direccion ?: "(vacío)")
                InfoField(label = "Ciudad", value = usuario.ciudad ?: "(vacío)")
                InfoField(label = "Código Postal", value = usuario.codigoPostal ?: "(vacío)")
            }
        }

        // Tarjeta: Información del Sistema
        InfoCard(title = "Información del Sistema") {
            InfoField(label = "Fecha de Creación", value = formatDate(usuario.fechaCreacion))
        }

        // Botones
        if (isEditMode) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onSave(nombreCompleto, telefono, direccion, ciudad, codigoPostal)
                        isEditMode = false
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        isEditMode = false
                        nombreCompleto = usuario.nombreCompleto
                        telefono = usuario.telefono ?: ""
                        direccion = usuario.direccion ?: ""
                        ciudad = usuario.ciudad ?: ""
                        codigoPostal = usuario.codigoPostal ?: ""
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        } else {

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private data class ImageUploadPayload(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String?
)

private fun extractImagePayload(
    contentResolver: android.content.ContentResolver,
    uri: android.net.Uri
): ImageUploadPayload? {
    val bitmap = contentResolver.openInputStream(uri)?.use { input ->
        android.graphics.BitmapFactory.decodeStream(input)
    } ?: return null

    val maxDimension = 1600
    val largestSide = maxOf(bitmap.width, bitmap.height)
    val preparedBitmap = if (largestSide > maxDimension) {
        val scale = maxDimension.toFloat() / largestSide.toFloat()
        android.graphics.Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
    } else {
        bitmap
    }

    val maxBytes = 900_000
    var quality = 88
    var compressedBytes: ByteArray
    do {
        val stream = ByteArrayOutputStream()
        preparedBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, stream)
        compressedBytes = stream.toByteArray()
        quality -= 8
    } while (compressedBytes.size > maxBytes && quality >= 40)

    return ImageUploadPayload(
        bytes = compressedBytes,
        fileName = "perfil_${System.currentTimeMillis()}.jpg",
        mimeType = "image/jpeg"
    )
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
private fun InfoField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
            ),
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
private fun ErrorContent(
    mensaje: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "Error al cargar perfil",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )

        Text(
            text = mensaje,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Reintentar")
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val dateTime = java.time.LocalDateTime.parse(dateString, formatter)
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        dateString
    }
}

