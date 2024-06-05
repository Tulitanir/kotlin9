package com.example.project.music

import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.squareup.picasso.Picasso


class MusicAdapter(
    val context: Context, private val dataList: List<Data>,
    val setCurrentMediaPlayer: (MediaPlayer) -> Unit
) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.music_item, parent, false)
        return MusicViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val currentData = dataList[position]
        val mediaPlayer = MediaPlayer.create(context, currentData.preview.toUri())

        holder.title.text = currentData.title
        Picasso.get().load(currentData.album.cover).into(holder.image)

        holder.play.setOnClickListener() {
            if (mediaPlayer.currentPosition == 0) {
                setCurrentMediaPlayer(mediaPlayer)
            }

            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }
        }

        holder.pause.setOnClickListener() {
            mediaPlayer.pause()
        }

        holder.stop.setOnClickListener() {
            mediaPlayer.stop()
            mediaPlayer.prepare()
        }
    }


    class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.musicImage)
        val title: TextView = itemView.findViewById(R.id.musicTitle)
        val play: ImageButton = itemView.findViewById(R.id.btnPlay)
        val pause: ImageButton = itemView.findViewById(R.id.btnPause)
        val stop: ImageButton = itemView.findViewById(R.id.btnStop)
    }
}