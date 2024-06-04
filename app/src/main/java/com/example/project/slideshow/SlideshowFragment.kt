package com.example.project.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.MainActivity
import com.example.project.R
import com.example.project.databinding.FragmentSlideshowBinding
import com.example.project.image.ImagePost
import com.example.project.image.PostAdapter
import com.example.project.image.PostInfo
import com.example.project.util.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects

class SlideshowFragment : Fragment() {

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
            .whereEqualTo("userId", MainActivity.DataManager.getId())
            .get()
            .addOnSuccessListener { result ->
                val data: MutableList<PostInfo> = mutableListOf()
                val posts = result.toObjects<ImagePost>()

                posts.forEach {post ->
                    post.userId?.let {
                        if (it == MainActivity.DataManager.getId()) {
                            data.add(PostInfo(post, MainActivity.DataManager.getUserData()?.login, MainActivity.DataManager.getUserData()?.image))
                        } else {
                            db.collection("users").document(it).get()
                                .addOnSuccessListener { doc ->
                                    if (doc.exists()) {
                                        val user = doc.toObject<User>()
                                        data.add(PostInfo(post, user?.login, user?.image))
                                    }
                                }
                        }
                    }
                }

                postAdapter = PostAdapter(view.context, data, true)
                recyclerView.adapter = postAdapter
                recyclerView.layoutManager = LinearLayoutManager(context)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Не удалось загрузить посты", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}