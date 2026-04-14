package com.example.myapplication.repository

import java.time.LocalDateTime

data class CacheInfo(
    val isOfflineMode: Boolean = false,
    val lastUpdatedAt: LocalDateTime? = null
)