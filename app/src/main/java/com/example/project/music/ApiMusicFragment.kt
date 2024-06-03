package com.example.project.music

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiMusicFragment : Fragment() {
    private val retrofitBuilder = Retrofit.Builder()
        .baseUrl("https://deezerdevs-deezer.p.rapidapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(MusicApiInterface::class.java)

    private lateinit var recyclerView: RecyclerView
    private lateinit var musicAdapter: MusicAdapter
    private var currentMediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_api_music, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retrofitData = retrofitBuilder.getData("Король и шут")

        retrofitData.enqueue(object : Callback<MusicData?> {
            override fun onResponse(p0: Call<MusicData?>, p1: Response<MusicData?>) {
                val data = p1.body()?.data!!

                musicAdapter = MusicAdapter(view.context, data, currentMediaPlayer)
                recyclerView.adapter = musicAdapter
                recyclerView.layoutManager = LinearLayoutManager(context)

                Log.d("onResponse", "onResponse: " + p1.body())
            }

            override fun onFailure(p0: Call<MusicData?>, p1: Throwable) {
                Log.d("onFailure", "onFailure: " + p1.message)
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = ApiMusicFragment()
    }
}