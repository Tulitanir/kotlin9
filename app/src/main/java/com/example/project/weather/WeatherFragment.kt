package com.example.project.weather

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.project.R
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherFragment : Fragment() {
    private val apiKey = "752ab3d3ea08df78c824c0ba11b34e78"
    private val city = "Saratov"
    private val retrofitBuilder = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApiInterface::class.java)

    private lateinit var cityName: TextView
    private lateinit var temperature: TextView
    private lateinit var description: TextView
    private lateinit var icon: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weather, container, false)

        cityName = view.findViewById(R.id.cityName)
        temperature = view.findViewById(R.id.temperature)
        description = view.findViewById(R.id.weatherDesc)
        icon = view.findViewById(R.id.icon)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retrofitBuilder.getCurrentWeatherData(city, apiKey)
            ?.enqueue(object : Callback<WeatherData?> {
                override fun onResponse(p0: Call<WeatherData?>, p1: Response<WeatherData?>) {
                    if (p1.isSuccessful) {
                        val data = p1.body()
                        if (data != null) {
                            cityName.text = data.cityName
                            val tempCelsius = data.main.temperature - 273.15
                            temperature.text = String.format("%.2f°C", tempCelsius)
                            if (data.weather.isNotEmpty()) {
                                val weather = data.weather[0]
                                description.text = weather.weatherDescription
                                val iconUrl =
                                    "http://openweathermap.org/img/wn/${weather.icon}@4x.png"
                                Picasso.get().load(iconUrl).into(icon)
                            }
                            Log.d("onResponse", data.toString())
                        } else {
                            Log.d("onResponse", "Response body is null")
                        }
                    } else {
                        Log.d("onResponse", "Response not successful: " + p1.errorBody()?.string())
                    }
                }

                override fun onFailure(p0: Call<WeatherData?>, p1: Throwable) {
                    Log.d("onFailure", "onFailure: " + p1.message)
                }
            })
    }
}
