package com.example.project.image

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface HuggingFaceApiInterface {
    @POST("models/cagliostrolab/animagine-xl-3.1")
    fun query(@Body payload: Map<String, String>): Call<ResponseBody>
}