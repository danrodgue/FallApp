package com.example.fallapp.presentation.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.fallapp.ui.theme.DarkText
import com.example.fallapp.ui.theme.OrangeAction
import com.example.fallapp.ui.theme.PeachSurface
import com.example.fallapp.ui.theme.RedAccent

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onAction: (RegisterAction) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar en esquina superior derecha
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(3.dp, RedAccent),
                    modifier = Modifier
                        .height(64.dp)
                        .clip(CircleShape),
                    color = PeachSurface
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = DarkText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                OutlinedTextField(
                    value = state.nombreCompleto,
                    onValueChange = { onAction(RegisterAction.NombreCompletoChanged(it)) },
                    label = { Text("Nombre completo", color = DarkText) },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PeachSurface,
                        unfocusedContainerColor = PeachSurface,
                        focusedIndicatorColor = RedAccent,
                        unfocusedIndicatorColor = PeachSurface,
                        cursorColor = DarkText
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onAction(RegisterAction.EmailChanged(it)) },
                    label = { Text("Email", color = DarkText) },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PeachSurface,
                        unfocusedContainerColor = PeachSurface,
                        focusedIndicatorColor = RedAccent,
                        unfocusedIndicatorColor = PeachSurface,
                        cursorColor = DarkText
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onAction(RegisterAction.PasswordChanged(it)) },
                    label = { Text("Contraseña", color = DarkText) },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PeachSurface,
                        unfocusedContainerColor = PeachSurface,
                        focusedIndicatorColor = RedAccent,
                        unfocusedIndicatorColor = PeachSurface,
                        cursorColor = DarkText
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.idFalla,
                    onValueChange = { onAction(RegisterAction.IdFallaChanged(it)) },
                    label = { Text("ID Falla (opcional)", color = DarkText) },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = PeachSurface,
                        unfocusedContainerColor = PeachSurface,
                        focusedIndicatorColor = RedAccent,
                        unfocusedIndicatorColor = PeachSurface,
                        cursorColor = DarkText
                    )
                )

                if (state.errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.errorMessage,
                        color = RedAccent
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onAction(RegisterAction.Submit) },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAction,
                        contentColor = Color.White
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = DarkText,
                            strokeWidth = 2.dp,
                            modifier = Modifier.height(20.dp)
                        )
                    } else {
                        Text(text = "Crear cuenta")
                    }
                }
            }
        }
    }
}

