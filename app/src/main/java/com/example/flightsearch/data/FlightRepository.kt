package com.example.flightsearch.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FlightRepository(private val flightDao: FlightDao) {
    fun searchAirports(query: String): Flow<List<Airport>> = flightDao.searchAirports(query)
    fun getAllDestinations(departureIata: String): Flow<List<Airport>> = flightDao.getAllDestinations(departureIata)
    fun getAllFavorites(): Flow<List<Favorite>> = flightDao.getAllFavorites()

    // Aquí es donde mandamos el trabajo a segundo plano de forma manual
    suspend fun insertFavorite(favorite: Favorite) = withContext(Dispatchers.IO) {
        flightDao.insertFavorite(favorite)
    }

    suspend fun deleteFavorite(favorite: Favorite) = withContext(Dispatchers.IO) {
        flightDao.deleteFavorite(favorite)
    }

    suspend fun getFavoriteRoute(departure: String, destination: String): Favorite? = withContext(Dispatchers.IO) {
        flightDao.getFavoriteRoute(departure, destination)
    }
}