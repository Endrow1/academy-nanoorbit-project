package com.example.myapplication.repository

import com.example.myapplication.api.NanoOrbitApi
import com.example.myapplication.data.local.FenetreDao
import com.example.myapplication.data.local.SatelliteDao
import com.example.myapplication.data.local.toEntity
import com.example.myapplication.data.local.toModel
import com.example.myapplication.models.FenetreCom
import com.example.myapplication.models.Instrument
import com.example.myapplication.models.Mission
import com.example.myapplication.models.Orbite
import com.example.myapplication.models.Satellite
import com.example.myapplication.models.StationSol
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NanoOrbitRepository(
    private val satelliteDao: SatelliteDao,
    private val fenetreDao: FenetreDao
) {
    /*
     * Lien explicite ALTN83 Q3 :
     * stratégie Cache-First = l'application lit d'abord le cache local Room,
     * puis tente une synchronisation réseau en arrière-plan.
     * Ainsi, même si le serveur central est indisponible, l'opérateur peut
     * continuer à consulter les dernières données disponibles.
     */

    private val gson = GsonBuilder()
        .registerTypeAdapter(
            LocalDateTime::class.java,
            JsonDeserializer { json, _, _ ->
                LocalDateTime.parse(
                    json.asString,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
            }
        )
        .create()

    private val api: NanoOrbitApi =
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8000/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(NanoOrbitApi::class.java)

    private val _cacheInfo = MutableStateFlow(CacheInfo())
    val cacheInfo: StateFlow<CacheInfo> = _cacheInfo

    fun observeSatellites(): Flow<List<Satellite>> =
        satelliteDao.observeAll().map { list -> list.map { it.toModel() } }

    fun observeFenetres(): Flow<List<FenetreCom>> =
        fenetreDao.observeAll().map { list -> list.map { it.toModel() } }

    suspend fun refreshSatellites() {
        try {
            delay(500)
            val now = LocalDateTime.now()
            val remoteSatellites = api.getSatellites()

            satelliteDao.clearAll()
            satelliteDao.insertAll(remoteSatellites.map { it.toEntity(now) })

            _cacheInfo.value = CacheInfo(
                isOfflineMode = false,
                lastUpdatedAt = now
            )
        } catch (e: Exception) {
            val hasCache = satelliteDao.count() > 0
            val lastUpdated = satelliteDao.getLastCachedAt()

            if (hasCache) {
                _cacheInfo.value = CacheInfo(
                    isOfflineMode = true,
                    lastUpdatedAt = lastUpdated
                )
            } else {
                throw e
            }
        }
    }

    suspend fun refreshFenetres() {
        try {
            delay(500)
            val now = LocalDateTime.now()
            val maxDate = now.plusDays(7)

            val remoteFenetres = api.getFenetres()
                .filter { !it.datetimeDebut.isBefore(now) && !it.datetimeDebut.isAfter(maxDate) }

            fenetreDao.clearAll()
            fenetreDao.insertAll(remoteFenetres.map { it.toEntity(now) })

            val current = _cacheInfo.value
            _cacheInfo.value = current.copy(
                isOfflineMode = false,
                lastUpdatedAt = now
            )
        } catch (e: Exception) {
            val lastUpdated = fenetreDao.getLastCachedAt()
            val current = _cacheInfo.value
            _cacheInfo.value = current.copy(
                isOfflineMode = true,
                lastUpdatedAt = lastUpdated ?: current.lastUpdatedAt
            )
        }
    }

    suspend fun getStations(): List<StationSol> {
        delay(300)
        return api.getStations()
    }

    suspend fun getOrbites(): List<Orbite> {
        delay(300)
        return api.getOrbites()
    }

    suspend fun getMissions(): List<Mission> {
        delay(300)
        return api.getMissions()
    }

    suspend fun getInstrumentsBySatellite(id: Int): List<Instrument> {
        delay(300)
        return api.getInstruments(id)
    }
}