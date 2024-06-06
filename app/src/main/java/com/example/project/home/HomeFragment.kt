package com.example.project.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.MainActivity
import com.example.project.R
import com.example.project.databinding.FragmentHomeBinding
import com.example.project.util.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()

        auth.signOut()
        MainActivity.DataManager.clearData()

        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_registationFragment)
        }

        binding.buttonLogin.setOnClickListener {
            val login = binding.editTextLogin.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            db.collection("users")
                .whereEqualTo("login", login)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val document = documents.documents[0]
                        val email = document.getString("email") ?: return@addOnSuccessListener
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    if (userId != null) {
                                        db.collection("users").document(userId).get()
                                            .addOnSuccessListener { doc ->
                                                if (doc.exists()) {
                                                    val user: User? = doc.toObject<User>()

                                                    if (user != null) {
                                                        MainActivity.DataManager.setId(doc.id)
                                                        MainActivity.DataManager.setUserData(user)

                                                        Log.d("ad", user.date!!)
                                                    }
                                                    findNavController().navigate(R.id.nav_slideshow)
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Пользователь не найден",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Ошибка входа: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Неверный логин или пароль", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}