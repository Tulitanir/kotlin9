package com.example.project.image

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.dialogs.ShowPostFragment
import com.example.project.dialogs.UpdatePostFragment
import com.example.project.music.MusicAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class PostAdapter(val context: Context,
                  private var dataList: MutableList<PostInfo>,
                  private val db: FirebaseFirestore,
                  private val fragmentManager: FragmentManager,
                  private val isEditable: Boolean = false):
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
        holder.image
            .setOnClickListener {
                val showDialog = ShowPostFragment(currentItem)
                showDialog.show(fragmentManager, "ShowPostDialog")
            }

        val pfp = currentItem.userPfp?.toBytes()
        if (pfp != null) {
            holder.userPfp.setImageBitmap(BitmapFactory.decodeByteArray(pfp, 0, pfp.size))
        }
        holder.login.text = currentItem.userLogin

        holder.delete.setOnClickListener {
            currentItem.post.imageUrl?.let { it1 -> deletePost(position, currentItem.postId, it1) }
        }

        holder.edit.setOnClickListener {
            val editDialog = UpdatePostFragment(currentItem) {editedItem ->
                db.collection("posts").document(currentItem.postId).set(editedItem.post)
                    .addOnSuccessListener {
                        dataList[position] = editedItem
                        notifyItemChanged(position)
                    }
            }
            editDialog.show(fragmentManager, "EditPostDialog")
        }

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

    private fun deletePost(position: Int, id: String, url: String) {
        val cloudStorage = FirebaseStorage.getInstance().getReferenceFromUrl(url)
        cloudStorage.delete()
            .addOnSuccessListener {
                db.collection("posts").document(id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Пост успешно удалён", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Не удалось удалить пост", Toast.LENGTH_SHORT).show()
                    }

                dataList.removeAt(position)
                notifyItemRemoved(position)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Не удалось удалить пост", Toast.LENGTH_SHORT).show()
            }


    }
}