package com.example.project.home

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.MainActivity
import com.example.project.R
import com.example.project.databinding.FragmentHomeBinding
import com.example.project.util.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        auth = FirebaseAuth.getInstance()
        auth.signOut()

        binding.buttonRegister.setOnClickListener {
            findNavController().navigate(R.id.action_nav_home_to_registationFragment)
        }

        binding.buttonLogin.setOnClickListener {
            val login = binding.editTextLogin.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val db = Firebase.firestore

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
                                                    val user: User? = doc.toObject(User::class.java)
                                                    if (user != null) {
                                                        Log.d("ad", user.date!!)
                                                    }

                                                    val header =
                                                        (requireActivity() as AppCompatActivity).findViewById<NavigationView>(R.id.nav_view)
                                                            ?.getHeaderView(0)
                                                    var text = header?.findViewById<TextView>(R.id.headertextNameSurname)
                                                    text!!.text = buildString {
                                                        append(doc["username"])
                                                        append(" ")
                                                        append(doc["usersurname"])
                                                    }
                                                    text = header?.findViewById(R.id.headertextLogin)
                                                    text!!.text = buildString {
                                                        append(doc["login"])
                                                    }
                                                    text = header?.findViewById(R.id.headertextDate)
                                                    text!!.text = buildString {
                                                        append(doc["date"])
                                                    }
                                                    if (doc["image"] != null) {
                                                        val blb: Blob = doc["image"] as Blob
                                                        val arr = blb.toBytes()
                                                        val bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.size)
                                                        val image = header?.findViewById<ImageView>(R.id.headerimageView)
                                                        image?.setImageBitmap(bitmap)
                                                    }
                                                    if (user != null) {
                                                        MainActivity.DataManager.setUserData(user)
                                                        MainActivity.DataManager.setId(doc.id)
                                                    }
                                                    findNavController().navigate(R.id.nav_slideshow)
                                                } else {
                                                    Toast.makeText(context, "Пользователь не найден", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                    }
                                } else {
                                    Toast.makeText(context, "Ошибка входа: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Неверный логин или пароль", Toast.LENGTH_SHORT).show()
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