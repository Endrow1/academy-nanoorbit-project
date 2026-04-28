package com.example.myapplication.repository

import com.example.myapplication.api.NanoOrbitApi
import com.example.myapplication.data.local.FenetreDao
import com.example.myapplication.data.local.InstrumentDao
import com.example.myapplication.data.local.MissionDao
import com.example.myapplication.data.local.OrbiteDao
import com.example.myapplication.data.local.SatelliteDao
import com.example.myapplication.data.local.StationDao
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
    private val fenetreDao: FenetreDao,
    private val orbiteDao: OrbiteDao,
    private val stationDao: StationDao,
    private val missionDao: MissionDao,
    private val instrumentDao: InstrumentDao
) {
    /*
     * Stratégie Cache-First :
     * 1. l'UI observe toujours Room
     * 2. on tente ensuite une synchro réseau
     * 3. en cas d'échec réseau, on continue d'afficher le cache
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
            .baseUrl("https://early-bushes-like.loca.lt/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(NanoOrbitApi::class.java)

    private val _cacheInfo = MutableStateFlow(CacheInfo())
    val cacheInfo: StateFlow<CacheInfo> = _cacheInfo

    fun observeSatellites(): Flow<List<Satellite>> =
        satelliteDao.observeAll().map { list -> list.map { it.toModel() } }

    fun observeFenetres(): Flow<List<FenetreCom>> =
        fenetreDao.observeAll().map { list -> list.map { it.toModel() } }

    fun observeOrbites(): Flow<List<Orbite>> =
        orbiteDao.observeAll().map { list -> list.map { it.toModel() } }

    fun observeStations(): Flow<List<StationSol>> =
        stationDao.observeAll().map { list -> list.map { it.toModel() } }

    fun observeMissions(): Flow<List<Mission>> =
        missionDao.observeAll().map { list -> list.map { it.toModel() } }

    fun observeInstrumentsBySatellite(satelliteId: Int): Flow<List<Instrument>> =
        instrumentDao.observeBySatelliteId(satelliteId).map { list -> list.map { it.toModel() } }

    suspend fun refreshAll() {
        try {
            delay(500)
            val now = LocalDateTime.now()
            now.plusDays(7)

            val remoteSatellites = api.getSatellites()
            val remoteFenetres = api.getFenetres()

            val remoteOrbites = api.getOrbites()
            val remoteStations = api.getStations()
            val remoteMissions = api.getMissions()

            satelliteDao.clearAll()
            satelliteDao.insertAll(remoteSatellites.map { it.toEntity(now) })

            fenetreDao.clearAll()
            fenetreDao.insertAll(remoteFenetres.map { it.toEntity(now) })

            orbiteDao.clearAll()
            orbiteDao.insertAll(remoteOrbites.map { it.toEntity(now) })

            stationDao.clearAll()
            stationDao.insertAll(remoteStations.map { it.toEntity(now) })

            missionDao.clearAll()
            missionDao.insertAll(remoteMissions.map { it.toEntity(now) })

            _cacheInfo.value = CacheInfo(
                isOfflineMode = false,
                lastUpdatedAt = now
            )
        } catch (e: Exception) {
            val hasAnyCache =
                satelliteDao.count() > 0 ||
                        fenetreDao.count() > 0 ||
                        orbiteDao.count() > 0 ||
                        stationDao.count() > 0 ||
                        missionDao.count() > 0 ||
                        instrumentDao.count() > 0

            if (hasAnyCache) {
                _cacheInfo.value = CacheInfo(
                    isOfflineMode = true,
                    lastUpdatedAt = getGlobalLastCachedAt()
                )
            } else {
                throw e
            }
        }
    }

    suspend fun refreshInstrumentsForSatellite(satelliteId: Int) {
        try {
            delay(300)
            val now = LocalDateTime.now()
            val remoteInstruments = api.getInstruments(satelliteId)

            instrumentDao.deleteBySatelliteId(satelliteId)
            instrumentDao.insertAll(remoteInstruments.map { it.toEntity(now) })

            val current = _cacheInfo.value
            _cacheInfo.value = current.copy(
                isOfflineMode = false,
                lastUpdatedAt = now
            )
        } catch (_: Exception) {
            val hasCache = instrumentDao.count() > 0
            if (hasCache) {
                val current = _cacheInfo.value
                _cacheInfo.value = current.copy(
                    isOfflineMode = true,
                    lastUpdatedAt = getGlobalLastCachedAt()
                )
            }
        }
    }

    private suspend fun getGlobalLastCachedAt(): LocalDateTime? {
        return listOfNotNull(
            satelliteDao.getLastCachedAt(),
            fenetreDao.getLastCachedAt(),
            orbiteDao.getLastCachedAt(),
            stationDao.getLastCachedAt(),
            missionDao.getLastCachedAt(),
            instrumentDao.getLastCachedAt()
        ).maxOrNull()
    }
}