package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.models.FenetreCom
import com.example.myapplication.models.Instrument
import com.example.myapplication.models.Mission
import com.example.myapplication.models.Orbite
import com.example.myapplication.models.Satellite
import com.example.myapplication.models.StationSol
import com.example.myapplication.models.StatutSatellite
import com.example.myapplication.repository.CacheInfo
import com.example.myapplication.repository.NanoOrbitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

class NanoOrbitViewModel(
    private val repository: NanoOrbitRepository
) : ViewModel() {

    private val _satellites = MutableStateFlow<List<Satellite>>(emptyList())
    private val _fenetres = MutableStateFlow<List<FenetreCom>>(emptyList())
    private val _stations = MutableStateFlow<List<StationSol>>(emptyList())
    private val _orbites = MutableStateFlow<List<Orbite>>(emptyList())
    private val _missions = MutableStateFlow<List<Mission>>(emptyList())
    private val _instruments = MutableStateFlow<List<Instrument>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatut = MutableStateFlow<StatutSatellite?>(null)
    private val _cacheInfo = MutableStateFlow(CacheInfo())

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedStatut: StateFlow<StatutSatellite?> = _selectedStatut.asStateFlow()
    val cacheInfo: StateFlow<CacheInfo> = _cacheInfo.asStateFlow()

    val fenetres: StateFlow<List<FenetreCom>> = _fenetres.asStateFlow()
    val stations: StateFlow<List<StationSol>> = _stations.asStateFlow()
    val orbites: StateFlow<List<Orbite>> = _orbites.asStateFlow()
    val missions: StateFlow<List<Mission>> = _missions.asStateFlow()
    val instruments: StateFlow<List<Instrument>> = _instruments.asStateFlow()

    val filteredSatellites: StateFlow<List<Satellite>> = combine(
        _satellites,
        _orbites,
        _searchQuery,
        _selectedStatut
    ) { sats, orbites, query, statut ->

        val normalizedQuery = query.trim().lowercase()

        sats.filter { sat ->
            val orbiteAssociee = orbites.find { it.idOrbite == sat.idOrbite }

            val matchesQuery =
                normalizedQuery.isBlank() ||
                        sat.nomSatellite.lowercase().contains(normalizedQuery) ||
                        orbiteAssociee?.typeOrbite?.lowercase()?.contains(normalizedQuery) == true

            val matchesStatut =
                statut == null || sat.statut == statut

            matchesQuery && matchesStatut
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        observeLocalCache()
        loadData()
    }

    private fun observeLocalCache() {
        viewModelScope.launch {
            repository.observeSatellites().collect {
                _satellites.value = it
            }
        }

        viewModelScope.launch {
            repository.observeFenetres().collect {
                _fenetres.value = it
            }
        }

        viewModelScope.launch {
            repository.observeStations().collect {
                _stations.value = it
            }
        }

        viewModelScope.launch {
            repository.observeOrbites().collect {
                _orbites.value = it
            }
        }

        viewModelScope.launch {
            repository.observeMissions().collect {
                _missions.value = it
            }
        }

        viewModelScope.launch {
            repository.cacheInfo.collect {
                _cacheInfo.value = it
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.refreshAll()
            } catch (e: Exception) {
                _errorMessage.value = "Erreur réseau : ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadInstrumentsForSatellite(satelliteId: Int) {
        viewModelScope.launch {
            repository.observeInstrumentsBySatellite(satelliteId).collect {
                _instruments.value = it
            }
        }

        viewModelScope.launch {
            repository.refreshInstrumentsForSatellite(satelliteId)
        }
    }

    fun refreshSatellites() {
        loadData()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatutFilterChange(statut: StatutSatellite?) {
        _selectedStatut.value = statut
    }

    fun getCacheAgeText(): String {
        val lastUpdate = _cacheInfo.value.lastUpdatedAt ?: return "Aucune donnée en cache"
        val duration = Duration.between(lastUpdate, LocalDateTime.now())

        return when {
            duration.toMinutes() < 1 -> "Mis à jour à l'instant"
            duration.toMinutes() < 60 -> "Mis à jour il y a ${duration.toMinutes()} min"
            duration.toHours() < 24 -> "Mis à jour il y a ${duration.toHours()} h"
            else -> "Mis à jour il y a ${duration.toDays()} jour(s)"
        }
    }
}