package com.example.flightsearch.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FlightDao {
    @Query("SELECT * FROM airport WHERE name LIKE '%' || :searchQuery || '%' OR iata_code LIKE '%' || :searchQuery || '%' ORDER BY passengers DESC")
    fun searchAirports(searchQuery: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE iata_code != :departureIata ORDER BY passengers DESC")
    fun getAllDestinations(departureIata: String): Flow<List<Airport>>

    @Query("SELECT * FROM favorite")
    fun getAllFavorites(): Flow<List<Favorite>>

    // IMPORTANTE: Ya NO tienen la palabra "suspend"
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorite(favorite: Favorite)

    @Delete
    fun deleteFavorite(favorite: Favorite)

    @Query("SELECT * FROM favorite WHERE departure_code = :departure AND destination_code = :destination")
    fun getFavoriteRoute(departure: String, destination: String): Favorite?
}