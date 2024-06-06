package com.example.project.dialogs

import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
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
import com.example.project.comment.CommentInfo
import com.example.project.comment.CommentsAdapter
import com.example.project.image.ImagePost
import com.example.project.image.PostInfo
import com.example.project.util.User
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.squareup.picasso.Picasso
import java.util.Locale

class ShowPostFragment(
    private var postInfo: PostInfo,
) : DialogFragment() {

    private lateinit var text: TextView
    private lateinit var date: TextView
    private lateinit var image: ImageView
    private lateinit var button: Button
    private lateinit var commentEditText: EditText
    private lateinit var recyclerView: RecyclerView
    private val data: MutableList<CommentInfo> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_post, container, false)
        text = view.findViewById(R.id.imageDescriptionView)
        text.text = postInfo.post.description
        date = view.findViewById(R.id.postDateTextView)

        val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        date.text = format.format(postInfo.post.date?.toDate())
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
                val userIds = mutableListOf<String>()
                val commentMap: MutableMap<String, MutableList<Pair<String, Comment>>> = mutableMapOf()

                for (document in result) {
                    val comment = document.toObject<Comment>()
                    val commentId = document.id
                    val userId = comment.userId

                    if (userId != null) {
                        userIds.add(userId)
                        commentMap.getOrPut(userId){ mutableListOf() }.add(commentId to comment)
                    }
                }

                if (userIds.isNotEmpty()) {
                    db.collection("users").whereIn(FieldPath.documentId(), userIds.toList())
                        .get()
                        .addOnSuccessListener { userResult ->
                            val userMap: MutableMap<String, User> = mutableMapOf()
                            for (userDocument in userResult.documents) {
                                val user = userDocument.toObject<User>()
                                if (user != null) {
                                    userMap[userDocument.id] = user
                                }
                            }

                            val currentUserId = MainActivity.DataManager.getId()
                            for ((userId, comments) in commentMap) {
                                val user = userMap[userId]
                                for ((commentId, comment) in comments) {
                                    user?.login?.let {
                                        CommentInfo(comment, commentId, postInfo.postId,
                                            it, user.image, userId == currentUserId)
                                    }?.let { data.add(it) }
                                }
                            }

                            recyclerView.adapter = CommentsAdapter(data)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Не удалось данные комментаторов", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    recyclerView.adapter = CommentsAdapter(data)
                }
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
            val user = MainActivity.DataManager.getUserData()
            val comment = Comment(userId = userId, text = commentText, date = Timestamp.now())

            db.collection("posts").document(postInfo.postId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener {
                    commentEditText.text.clear()
                    if (user != null) {
                        user.login?.let { it1 ->
                            CommentInfo(comment, it.id, postInfo.postId,
                                it1, user.image, true)
                        }?.let { it2 -> data.add(it2) }
                    }
                    recyclerView.adapter?.notifyItemInserted(data.size - 1)
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