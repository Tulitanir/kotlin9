package com.example.project.image

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.music.MusicAdapter
import com.squareup.picasso.Picasso

class PostAdapter(val context: Context, private val dataList: List<PostInfo>, private val isEditable: Boolean = false):
    RecyclerView.Adapter<PostAdapter.PostViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.post_item, parent, false)
        return PostViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val currentItem = dataList[position]

        Picasso.get().load(currentItem.post.imageUrl).into(holder.image)
        val pfp = currentItem.userPfp?.toBytes()
        if (pfp != null) {
            holder.userPfp.setImageBitmap(BitmapFactory.decodeByteArray(pfp, 0, pfp.size))
        }
        holder.login.text = currentItem.userLogin

        if (!isEditable) {
            holder.edit.visibility = View.GONE
            holder.delete.visibility = View.GONE
        }
    }

    class PostViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.postImage)
        val userPfp: ImageView = itemView.findViewById(R.id.userImage)
        val login: TextView = itemView.findViewById(R.id.userLoginText)
        val edit: ImageButton = itemView.findViewById(R.id.btnEdit)
        val delete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }
}