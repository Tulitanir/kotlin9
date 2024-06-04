package com.example.project.image

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.MainActivity
import com.example.project.R
import com.example.project.util.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class AllPostsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_slideshow, container, false)

        recyclerView = view.findViewById(R.id.recycler_view_slideshow)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore
        db.collection("posts")
            .get()
            .addOnSuccessListener { result ->
                val data: MutableList<PostInfo> = mutableListOf()
                val documents = result.documents
                val tasks = mutableListOf<Task<DocumentSnapshot?>>()

                for (document in documents) {
                    val postId = document.id
                    val post = document.toObject<ImagePost>()

                    Log.d("Docs: ", document.toString())

                    if (post != null) {
                        post.userId?.let {
                            if (it == MainActivity.DataManager.getId()) {
                                data.add(
                                    PostInfo(
                                        postId,
                                        post,
                                        MainActivity.DataManager.getUserData()?.login,
                                        MainActivity.DataManager.getUserData()?.image
                                    )
                                )
                            } else {
                                val userTask = db.collection("users").document(it).get()
                                    .addOnSuccessListener { doc ->
                                        if (doc.exists()) {
                                            val user = doc.toObject<User>()
                                            data.add(PostInfo(postId, post, user?.login, user?.image))
                                            Log.d("Doc user: ", user.toString())
                                        }
                                    }
                                    .addOnFailureListener {ex ->
                                        ex.message?.let { it1 -> Log.d("Doc: ", it1) }
                                    }
                                tasks.add(userTask)
                            }
                        }
                    }
                }

                Tasks.whenAllSuccess<DocumentSnapshot?>(tasks)
                    .addOnSuccessListener {
                        Log.d("Data: ", data.toString())
                        postAdapter = PostAdapter(view.context, data, db, parentFragmentManager)
                        recyclerView.adapter = postAdapter
                        recyclerView.layoutManager = LinearLayoutManager(context)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Не удалось загрузить посты", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Не удалось загрузить посты", Toast.LENGTH_SHORT).show()
            }
    }
}