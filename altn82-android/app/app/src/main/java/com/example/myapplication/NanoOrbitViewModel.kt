package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.mock.MockData
import com.example.myapplication.models.Satellite
import com.example.myapplication.models.StatutSatellite
import com.example.myapplication.repository.NanoOrbitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NanoOrbitViewModel(
    private val repository: NanoOrbitRepository = NanoOrbitRepository()
) : ViewModel() {

    private val _satellites = MutableStateFlow<List<Satellite>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _selectedStatut = MutableStateFlow<StatutSatellite?>(null)

    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    val selectedStatut: StateFlow<StatutSatellite?> = _selectedStatut.asStateFlow()

    val filteredSatellites: StateFlow<List<Satellite>> = combine(
        _satellites, _searchQuery, _selectedStatut
    ) { sats, query, statut ->
        sats.filter { sat ->
            val matchesQuery = sat.nomSatellite.contains(
                query,
                ignoreCase = true
            ) || MockData.orbites.find { it.idOrbite == sat.idOrbite }?.typeOrbite?.contains(
                query,
                ignoreCase = true
            ) == true

            val matchesStatut = statut == null || sat.statut == statut

            matchesQuery && matchesStatut
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadSatellites()
    }


    fun loadSatellites() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = repository.getSatellites()
                _satellites.value = response
            } catch (e: Exception) {
                _errorMessage.value = "Erreur réseau : " + e.message
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

    fun validateDuree(duree: Int): Boolean {
        return if (duree in 1..900) {
            true
        } else {
            _errorMessage.value = "La durée doit être comprise entre 1 et 900 secondes"
            false
        }
    }
}