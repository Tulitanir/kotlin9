package com.example.project.dialogs

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.MainActivity
import com.example.project.R
import com.example.project.comment.Comment
import com.example.project.comment.CommentsAdapter
import com.example.project.image.PostInfo
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import com.squareup.picasso.Picasso

class ShowPostFragment(
    private var postInfo: PostInfo,
) : DialogFragment() {

    private lateinit var text: TextView
    private lateinit var image: ImageView
    private lateinit var button: Button
    private lateinit var commentEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var comments: MutableList<Comment>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_post, container, false)
        text = view.findViewById(R.id.imageDescriptionView)
        text.text = postInfo.post.description
        image = view.findViewById(R.id.uploadedImage)
        Picasso.get().load(postInfo.post.imageUrl).into(image)
        button = view.findViewById(R.id.exitButton)
        button.setOnClickListener {
            postComment()
        }
        commentEditText = view.findViewById(R.id.commentEditText)

        recyclerView = view.findViewById(R.id.commentsRecyclerView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.layoutManager = LinearLayoutManager(context)
        loadComments()
    }

    private fun loadComments() {
        val db = Firebase.firestore
        db.collection("posts").document(postInfo.postId)
            .collection("comments")
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val comments = result.toObjects<Comment>()
                recyclerView.adapter = CommentsAdapter(comments)
            }
            .addOnFailureListener { e ->
                Log.w("Comments get: ", "Error getting comments", e)
            }
    }

    private fun postComment() {
        val commentText = commentEditText.text.toString()
        if (commentText.isNotEmpty()) {
            val db = Firebase.firestore
            val userId = MainActivity.DataManager.getId()
            val comment = Comment(userId = userId, text = commentText, date = Timestamp.now())

            db.collection("posts").document(postInfo.postId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener {
                    commentEditText.text.clear()
                    loadComments()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Не удалось добавить комментарий", Toast.LENGTH_SHORT).show()
                    Log.w("Comment upload: ", "Error posting comment", e)
                }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}