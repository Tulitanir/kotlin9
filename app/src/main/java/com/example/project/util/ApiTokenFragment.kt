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
import com.example.project.R

class ApiTokenFragment : Fragment() {

    private lateinit var apiTokenEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_api_token, container, false)
        apiTokenEditText = view.findViewById(R.id.apiTokenEditText)
        saveButton = view.findViewById(R.id.saveButton)

        val existingToken = getTokenFromPreferences(requireContext())
        if (existingToken.isNotEmpty()) {
            apiTokenEditText.setText(existingToken)
        }

        saveButton.setOnClickListener {
            val token = apiTokenEditText.text.toString()
            if (token.isNotEmpty()) {
                saveTokenToPreferences(requireContext(), token)
                Toast.makeText(requireContext(), "API токен сохранён", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Пожалуйста, введите API токен", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun saveTokenToPreferences(context: Context, token: String) {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("api_token", token).apply()
    }

    private fun getTokenFromPreferences(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("api_token", "") ?: ""
    }

    companion object {
        @JvmStatic
        fun newInstance() = ApiTokenFragment()
    }
}
