package com.example.project.music

import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
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
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var progressBar: ProgressBar
    private var currentMediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_api_music, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        searchEditText = view.findViewById(R.id.searchEditText)
        searchButton = view.findViewById(R.id.searchButton)
        progressBar = view.findViewById(R.id.progressBarMusic)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchButton.setOnClickListener {
            clearMusicPlayer()
            val query = searchEditText.text.toString().trim()
            if (query.isNotBlank()) {
                recyclerView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                fetchMusicData(query)
            } else {
                Toast.makeText(context, "Пожалуйста, введите запрос", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearMusicPlayer() {
        currentMediaPlayer?.stop()
        currentMediaPlayer?.prepare()
        currentMediaPlayer = null
    }

    private fun setCurrentMediaPlayer(mediaPlayer: MediaPlayer) {
        clearMusicPlayer()
        currentMediaPlayer = mediaPlayer
    }

    private fun fetchMusicData(query: String) {
        val retrofitData = retrofitBuilder.getData(query)

        retrofitData.enqueue(object : Callback<MusicData?> {
            override fun onResponse(call: Call<MusicData?>, response: Response<MusicData?>) {
                val data = response.body()?.data!!
                musicAdapter = MusicAdapter(view!!.context, data, ::setCurrentMediaPlayer)
                recyclerView.adapter = musicAdapter
                recyclerView.layoutManager = LinearLayoutManager(context)
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                Log.d("onResponse", "onResponse: " + response.body())
            }

            override fun onFailure(call: Call<MusicData?>, t: Throwable) {
                Log.d("onFailure", "onFailure: " + t.message)
            }
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() = ApiMusicFragment()
    }
}
