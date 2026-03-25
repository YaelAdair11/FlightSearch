package com.example.flightsearch

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.flightsearch.datos.FlightDatabase
import com.example.flightsearch.datos.FlightRepository
import com.example.flightsearch.datos.UserPreferencesRepository

// Instancia única de DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "flight_preferences")

class FlightSearchApplication : Application() {
    lateinit var flightRepository: FlightRepository
    lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate() {
        super.onCreate()
        // Inicializamos las bases de datos al abrir la app
        val database = FlightDatabase.getDatabase(this)
        flightRepository = FlightRepository(database.flightDao())
        userPreferencesRepository = UserPreferencesRepository(dataStore)
    }
}