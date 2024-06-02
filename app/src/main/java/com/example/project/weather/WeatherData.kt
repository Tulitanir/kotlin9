package com.example.project.weather

import com.google.gson.annotations.SerializedName

data class WeatherData(
    @SerializedName("name")
    val cityName: String,

    @SerializedName("main")
    val main: Main,

    @SerializedName("weather")
    val weather: List<Weather>
) {
    data class Main(
        @SerializedName("temp")
        val temperature: Float
    )

    data class Weather(
        @SerializedName("description")
        val weatherDescription: String,

        @SerializedName("icon")
        val icon: String
    )
}

