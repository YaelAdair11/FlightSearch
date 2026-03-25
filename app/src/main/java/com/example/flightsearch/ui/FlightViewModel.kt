package com.example.flightsearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.flightsearch.FlightSearchApplication
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FlightRepository
import com.example.flightsearch.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlightViewModel(
    private val flightRepository: FlightRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    // Guarda el texto de búsqueda actual
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        // Al iniciar, lee la última búsqueda guardada en DataStore
        viewModelScope.launch {
            userPreferencesRepository.searchQuery.collect { savedQuery ->
                _searchQuery.value = savedQuery
            }
        }
    }

    // Actualiza el texto y lo guarda en DataStore
    fun updateQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            userPreferencesRepository.saveSearchQuery(query)
        }
    }

    // Funciones para leer la base de data desde la UI
    fun searchAirports(query: String) = flightRepository.searchAirports(query)
    fun getDestinations(departureIata: String) = flightRepository.getAllDestinations(departureIata)
    fun getFavorites() = flightRepository.getAllFavorites()

    // Funciones para manejar favoritos
    fun toggleFavorite(departureCode: String, destinationCode: String) {
        viewModelScope.launch {
            val existingFavorite = flightRepository.getFavoriteRoute(departureCode, destinationCode)
            if (existingFavorite != null) {
                flightRepository.deleteFavorite(existingFavorite)
            } else {
                flightRepository.insertFavorite(
                    Favorite(departureCode = departureCode, destinationCode = destinationCode)
                )
            }
        }
    }

    // Factory para inicializar el ViewModel con sus dependencias
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as FlightSearchApplication)
                FlightViewModel(
                    flightRepository = application.flightRepository,
                    userPreferencesRepository = application.userPreferencesRepository
                )
            }
        }
    }
}