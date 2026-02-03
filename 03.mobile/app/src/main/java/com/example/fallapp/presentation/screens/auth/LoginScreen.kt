package com.example.fallapp.presentation.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.fallapp.ui.theme.DarkText
import com.example.fallapp.ui.theme.OrangeAction
import com.example.fallapp.ui.theme.PeachSurface
import com.example.fallapp.ui.theme.RedAccent

@Composable
fun LoginScreen(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit
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
            // Cabecera / logo (simplificado)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(3.dp, RedAccent),
                    color = PeachSurface,
                    modifier = Modifier
                        .height(96.dp)
                        .fillMaxWidth(0.6f)
                ) {}

                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "FallApp", color = DarkText)
            }

            Column {
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { onAction(LoginAction.EmailChanged(it)) },
                    label = { Text("Email") },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
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
                    onClick = { onAction(LoginAction.Submit) },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAction
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = DarkText,
                            strokeWidth = 2.dp,
                            modifier = Modifier.height(20.dp)
                        )
                    } else {
                        Text(text = "Iniciar sesión")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { onAction(LoginAction.NavigateToRegister) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PeachSurface
                    )
                ) {
                    Text(text = "Registrarse", color = DarkText)
                }
            }
        }
    }
}

