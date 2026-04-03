package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.mock.MockData
import com.example.myapplication.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NanoOrbitViewModel : ViewModel() {

    private val _satellites = MutableStateFlow<List<Satellite>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatut = MutableStateFlow<StatutSatellite?>(null)

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredSatellites: StateFlow<List<Satellite>> = combine(
        _satellites, _searchQuery, _selectedStatut
    ) { sats, query, statut ->
        sats.filter { sat ->
            val matchesQuery = sat.nomSatellite.contains(query, ignoreCase = true) ||
                    MockData.orbites.find { it.idOrbite == sat.idOrbite }?.typeOrbite?.contains(
                        query,
                        ignoreCase = true
                    ) == true
            val matchesStatut = statut == null || sat.statut == statut
            matchesQuery && matchesStatut
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadSatellites()
    }

    fun loadSatellites() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                delay(1000) // TODO remove
                _satellites.value = MockData.satellites
            } catch (e: Exception) {
                _errorMessage.value = "Erreur lors du chargement"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatutFilterChange(statut: StatutSatellite?) {
        _selectedStatut.value = statut
    }

    fun refreshSatellites() {
        loadSatellites()
    }
}