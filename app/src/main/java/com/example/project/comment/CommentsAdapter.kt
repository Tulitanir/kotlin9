package com.example.project.comment

import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.squareup.picasso.Picasso
import java.util.Locale

class CommentsAdapter(private val comments: MutableList<CommentInfo>) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comment_item, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val commentInfo = comments[position]
        holder.userTextView.text = commentInfo.userLogin
        holder.commentTextView.text = commentInfo.comment.text
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        holder.dateTextView.text = format.format(commentInfo.comment.date.toDate())

        if (commentInfo.userPfpUrl != null) {
            Picasso.get().load(commentInfo.userPfpUrl).into(holder.userImageView)
        }


        if (!commentInfo.isEditable) {
            holder.deleteButton.visibility = View.GONE
        }

        holder.deleteButton.setOnClickListener {
            val db = Firebase.firestore
            db.collection("posts").document(commentInfo.postId)
                .collection("comments").document(commentInfo.commentId)
                .delete()
                .addOnSuccessListener {
                    comments.removeAt(position)
                    notifyItemRemoved(position)
                }
                .addOnFailureListener { e ->
                    Log.w("Comment del: ", "Error deleting comment", e)
                }
        }
    }

    override fun getItemCount() = comments.size

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userTextView: TextView = view.findViewById(R.id.commentUserTextView)
        val commentTextView: TextView = view.findViewById(R.id.commentTextView)
        val dateTextView: TextView = view.findViewById(R.id.commentDateTextView)
        val userImageView: ImageView = view.findViewById(R.id.commentUserImageView)
        val deleteButton: Button = view.findViewById(R.id.deleteCommentButton)
    }
}