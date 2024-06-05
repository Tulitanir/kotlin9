package com.example.project.util

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.R
import com.google.firebase.auth.FirebaseAuth

class ApiTokenFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var apiTokenEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var exitButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_api_token, container, false)
        auth = FirebaseAuth.getInstance()
        apiTokenEditText = view.findViewById(R.id.apiTokenEditText)
        saveButton = view.findViewById(R.id.saveButton)
        exitButton = view.findViewById(R.id.exitButton)

        saveButton.setOnClickListener {
            val token = apiTokenEditText.text.toString()
            if (token.isNotEmpty()) {
                saveTokenToPreferences(requireContext(), token)
                Toast.makeText(requireContext(), "API токен сохранён", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Пожалуйста, введите API токен",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        exitButton.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.nav_home)
        }

        return view
    }

    private fun saveTokenToPreferences(context: Context, token: String) {
        val sharedPreferences =
            context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("api_token", token).apply()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ApiTokenFragment()
    }
}
