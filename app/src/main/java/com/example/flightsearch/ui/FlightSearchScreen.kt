package com.example.flightsearch.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.width

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightSearchScreen(
    viewModel: FlightViewModel = viewModel(factory = FlightViewModel.Factory)
) {
    // Leemos la búsqueda guardada en Preferences DataStore
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Estado local: qué aeropuerto tocaste para ver sus vuelos
    var aeropuertoSeleccionado by remember { mutableStateOf<com.example.flightsearch.data.Airport?>(null) }

    // Recolectamos data de Room automáticamente
    val sugerencias by viewModel.searchAirports(searchQuery).collectAsState(initial = emptyList())
    val favoritos by viewModel.getFavorites().collectAsState(initial = emptyList())
    val destinos by viewModel.getDestinations(aeropuertoSeleccionado?.iataCode ?: "").collectAsState(initial = emptyList())

    // Si el usuario borra la búsqueda, nos salimos del aeropuerto seleccionado
    LaunchedEffect(searchQuery) {
        if (searchQuery.isEmpty()) {
            aeropuertoSeleccionado = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Búsqueda de Vuelos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- BARRA DE BÚSQUEDA ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    viewModel.updateQuery(it)
                    aeropuertoSeleccionado = null // Si escribe algo nuevo, volvemos a mostrar sugerencias
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ingresa el aeropuerto o código IATA") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- LÓGICA DINÁMICA DE LA PANTALLA ---
            if (searchQuery.isEmpty()) {
                // ESTADO 1: Sin búsqueda -> Mostrar Favoritos
                Text("Rutas Favoritas", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (favoritos.isEmpty()) {
                    Text("No tienes rutas favoritas aún.", color = Color.Gray)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(favoritos) { favorito ->
                            TarjetaVuelo(
                                salida = favorito.departureCode,
                                destino = favorito.destinationCode,
                                esFavorito = true,
                                alHacerClicFavorito = { viewModel.toggleFavorite(favorito.departureCode, favorito.destinationCode) }
                            )
                        }
                    }
                }
            } else if (aeropuertoSeleccionado == null) {
                // ESTADO 2: Escribiendo -> Mostrar Sugerencias (Autocompletar)
                LazyColumn {
                    items(sugerencias) { aeropuerto ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { aeropuertoSeleccionado = aeropuerto }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = aeropuerto.iataCode, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp))
                            Text(text = aeropuerto.name)
                        }
                        Divider()
                    }
                }
            } else {
                // ESTADO 3: Aeropuerto seleccionado -> Mostrar todos los vuelos
                Text("Vuelos desde ${aeropuertoSeleccionado!!.iataCode}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(destinos) { destino ->
                        // Revisamos si esta ruta en particular ya está guardada en favoritos
                        val esFavorito = favoritos.any { it.departureCode == aeropuertoSeleccionado!!.iataCode && it.destinationCode == destino.iataCode }

                        TarjetaVuelo(
                            salida = aeropuertoSeleccionado!!.iataCode,
                            destino = destino.iataCode,
                            nombreSalida = aeropuertoSeleccionado!!.name,
                            nombreDestino = destino.name,
                            esFavorito = esFavorito,
                            alHacerClicFavorito = { viewModel.toggleFavorite(aeropuertoSeleccionado!!.iataCode, destino.iataCode) }
                        )
                    }
                }
            }
        }
    }
}

// Componente visual para reutilizar el diseño de las tarjetas de vuelo
@Composable
fun TarjetaVuelo(
    salida: String,
    destino: String,
    nombreSalida: String = "",
    nombreDestino: String = "",
    esFavorito: Boolean,
    alHacerClicFavorito: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("SALIDA", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(salida, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (nombreSalida.isNotEmpty()) Text(" - $nombreSalida", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("LLEGADA", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(destino, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (nombreDestino.isNotEmpty()) Text(" - $nombreDestino", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                }
            }

            IconButton(onClick = alHacerClicFavorito) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorito",
                    tint = if (esFavorito) Color(0xFFFFC107) else Color.LightGray
                )
            }
        }
    }
}