package com.example.myapplication.data.local

import androidx.room.TypeConverter
import com.example.myapplication.models.MissionDTO
import com.example.myapplication.models.StatutFenetre
import com.example.myapplication.models.StatutSatellite
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import kotlin.collections.emptyList

class RoomConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? =
        value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromStatutSatellite(value: StatutSatellite): String = value.name

    @TypeConverter
    fun toStatutSatellite(value: String): StatutSatellite =
        StatutSatellite.valueOf(value)

    @TypeConverter
    fun fromStatutFenetre(value: StatutFenetre): String = value.name

    @TypeConverter
    fun toStatutFenetre(value: String): StatutFenetre =
        StatutFenetre.valueOf(value)

    @TypeConverter
    fun fromMissionDtoList(value: List<MissionDTO>?): String {
        return gson.toJson(value ?: emptyList<MissionDTO>())
    }

    @TypeConverter
    fun toMissionDtoList(value: String?): List<MissionDTO> {
        if (value.isNullOrBlank()) return emptyList()
        val type = object : TypeToken<List<MissionDTO>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

}