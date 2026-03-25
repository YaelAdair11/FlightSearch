package com.example.flightsearch.datos

import kotlinx.coroutines.flow.Flow

class FlightRepository(private val flightDao: FlightDao) {
    fun searchAirports(query: String): Flow<List<Airport>> = flightDao.searchAirports(query)
    fun getAllDestinations(departureIata: String): Flow<List<Airport>> = flightDao.getAllDestinations(departureIata)
    fun getAllFavorites(): Flow<List<Favorite>> = flightDao.getAllFavorites()

    suspend fun insertFavorite(favorite: Favorite) = flightDao.insertFavorite(favorite)
    suspend fun deleteFavorite(favorite: Favorite) = flightDao.deleteFavorite(favorite)
    suspend fun getFavoriteRoute(departure: String, destination: String): Favorite? = flightDao.getFavoriteRoute(departure, destination)
}