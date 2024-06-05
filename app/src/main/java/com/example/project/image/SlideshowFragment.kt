package com.example.project.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.MainActivity
import com.example.project.R
import com.example.project.util.User
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

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

        (activity as MainActivity).userDataLoaded.observe(viewLifecycleOwner) { isLoaded ->
            if (isLoaded) {
                loadPosts()
            }
        }
    }

    private fun loadPosts() {
        val db = Firebase.firestore
        val query = db.collection("posts")
            .whereEqualTo("userId", MainActivity.DataManager.getId())

        query.get()
            .addOnSuccessListener { result ->
                val data: MutableList<PostInfo> = mutableListOf()
                val userIds: MutableSet<String> = mutableSetOf()
                val postMap: MutableMap<String, MutableList<Pair<String, ImagePost>>> = mutableMapOf()

                // Сначала собираем все userId из постов
                for (document in result.documents) {
                    val post = document.toObject<ImagePost>()
                    if (post != null) {
                        val postId = document.id
                        val userId = post.userId
                        if (userId != null) {
                            userIds.add(userId)
                            postMap.getOrPut(userId) { mutableListOf() }.add(postId to post)
                        }
                    }
                }

                // Загружаем данные пользователей одним запросом
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

                            // Теперь создаем PostInfo объекты
                            for ((userId, posts) in postMap) {
                                val user = userMap[userId]
                                for ((postId, post) in posts) {
                                    data.add(PostInfo(postId = postId, post = post, userLogin = user?.login, userPfp = user?.image))
                                }
                            }

                            // Обновляем адаптер после загрузки данных
                            postAdapter = PostAdapter(requireView().context, data, db, parentFragmentManager, true)
                            recyclerView.adapter = postAdapter
                            recyclerView.layoutManager = LinearLayoutManager(context)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Не удалось загрузить данные пользователей", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Обновляем адаптер если нет постов с userId
                    postAdapter = PostAdapter(requireView().context, data, db, parentFragmentManager, true)
                    recyclerView.adapter = postAdapter
                    recyclerView.layoutManager = LinearLayoutManager(context)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Не удалось загрузить посты", Toast.LENGTH_SHORT).show()
            }
    }
}