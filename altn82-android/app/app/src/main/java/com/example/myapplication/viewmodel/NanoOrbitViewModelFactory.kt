package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.repository.NanoOrbitRepository

class NanoOrbitViewModelFactory(
    private val repository: NanoOrbitRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NanoOrbitViewModel::class.java)) {
            return NanoOrbitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}