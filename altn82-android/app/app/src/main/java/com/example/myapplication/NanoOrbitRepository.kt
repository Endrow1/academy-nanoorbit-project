package com.example.myapplication.repository

import com.example.myapplication.api.NanoOrbitApi
import com.example.myapplication.models.Satellite
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import kotlinx.coroutines.delay
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NanoOrbitRepository {

    private val gson = GsonBuilder()
        .registerTypeAdapter(
            LocalDateTime::class.java,
            JsonDeserializer { json, _, _ ->
                LocalDateTime.parse(
                    json.asString,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
            })
        .create()
    
    private val api: NanoOrbitApi =
        Retrofit.Builder().baseUrl("https://nano-orbit-api.loca.lt/")
            .addConverterFactory(
                GsonConverterFactory.create(
                    gson
                )
            )
            .build().create(NanoOrbitApi::class.java)

    suspend fun getSatellites(): List<Satellite> {
        delay(2000)
        return api.getSatellites()
    }

}