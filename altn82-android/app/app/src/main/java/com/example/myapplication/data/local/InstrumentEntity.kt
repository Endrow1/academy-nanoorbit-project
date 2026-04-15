package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.models.Instrument
import com.example.myapplication.models.StatutInstrument
import java.time.LocalDateTime

@Entity(tableName = "instruments")
data class InstrumentEntity(
    @PrimaryKey val refInstrument: String,
    val typeInstrument: String,
    val modele: String,
    val resolution: String?,
    val consommation: Double?,
    val idSatellite: Int,
    val statutInstrument: StatutInstrument,
    val cachedAt: LocalDateTime
)

fun Instrument.toEntity(cachedAt: LocalDateTime): InstrumentEntity = InstrumentEntity(
    refInstrument = refInstrument,
    typeInstrument = typeInstrument,
    modele = modele,
    resolution = resolution,
    consommation = consommation,
    idSatellite = idSatellite,
    statutInstrument = statutInstrument,
    cachedAt = cachedAt
)

fun InstrumentEntity.toModel(): Instrument = Instrument(
    refInstrument = refInstrument,
    typeInstrument = typeInstrument,
    modele = modele,
    resolution = resolution,
    consommation = consommation,
    idSatellite = idSatellite,
    statutInstrument = statutInstrument
)