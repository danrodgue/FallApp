package com.example.fallapp.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import com.example.fallapp.ui.theme.DarkText

@Composable
fun HomeScreen() {
    val items = listOf("Mapa", "Ninots", "Fallas")
    val selectedIndex = remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, label ->
                    NavigationBarItem(
                        selected = selectedIndex.value == index,
                        onClick = { selectedIndex.value = index },
                        icon = {
                            val icon = when (index) {
                                0 -> Icons.Filled.Map
                                1 -> Icons.Filled.Star
                                else -> Icons.Filled.List
                            }
                            Icon(imageVector = icon, contentDescription = label)
                        },
                        label = { Text(text = label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (selectedIndex.value) {
                0 -> {
                    Text(
                        text = "Mapa (placeholder)\nAquí iría el mapa con las fallas.",
                        color = DarkText,
                        fontSize = 18.sp
                    )
                }

                1 -> {
                    Text(
                        text = "Listado de ninots (placeholder)\nAquí iría la lista y detalle de ninots, filtros y votos.",
                        color = DarkText,
                        fontSize = 18.sp
                    )
                }

                2 -> {
                    Text(
                        text = "Listado de fallas (placeholder)\nAquí iría la lista de fallas y sus eventos.",
                        color = DarkText,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "La lógica de datos (Room + Retrofit + offline-first)\nse implementará en los ViewModels de cada pestaña.",
                color = DarkText,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

