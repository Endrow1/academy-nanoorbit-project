package com.example.myapplication.api

import com.example.myapplication.models.FenetreCom
import com.example.myapplication.models.Instrument
import com.example.myapplication.models.Satellite
import retrofit2.http.GET
import retrofit2.http.Path

interface NanoOrbitApi {

    @GET("satellites")
    suspend fun getSatellites(): List<Satellite>

    @GET("satellites/{id}/instruments")
    suspend fun getInstruments(@Path("id") id: Int): List<Instrument>

    @GET("fenetres")
    suspend fun getFenetres(): List<FenetreCom>
}