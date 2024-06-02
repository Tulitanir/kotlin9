package com.example.project.weather

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApiInterface {
    @GET("weather")
    fun getCurrentWeatherData(
        @Query("q") location: String?,
        @Query("appid") apiKey: String?
    ): Call<WeatherData?>?
}