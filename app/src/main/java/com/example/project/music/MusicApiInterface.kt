package com.example.project.music

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface MusicApiInterface {

    @Headers(
        "x-rapidapi-key: 13cb6126d4msh04e4cf8fb142ce8p14c05djsn9cd150b7ad9b",
        "x-rapidapi-host: deezerdevs-deezer.p.rapidapi.com"
    )
    @GET("search")
    fun getData(@Query("q") query: String): Call<MusicData>
}