package com.example.project.registration

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.MainActivity
import com.example.project.databinding.FragmentRegistationBinding
import com.example.project.util.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Calendar
import java.util.UUID


class RegistrationFragment : Fragment() {

    private lateinit var imagebytes: ByteArray
    private lateinit var uri: Uri
    private var date = Calendar.getInstance()
    private var _binding: FragmentRegistationBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistationBinding.inflate(inflater, container, false)
        val root: View = binding.root
        auth = FirebaseAuth.getInstance()

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                uri = it
            }
        }

        binding.button2.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.textdate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.button3.setOnClickListener {
            if (areFieldsValid()) {
                val email = binding.textEmail.text.toString().trim()
                val password = binding.textPass.text.toString().trim()

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = auth.currentUser?.uid
                            val db = Firebase.firestore
                            FirebaseFirestore.setLoggingEnabled(true)

                            val userbld = User.Builder()
                                .username(binding.textName.text.toString().trim())
                                .usersurname(binding.textSurname.text.toString().trim())
                                .login(binding.textLogin.text.toString().trim())
                                .email(email)
                                .password(password)
                                .date(binding.textdate.text.toString().trim())

                            if (this::uri.isInitialized) {
                                val cloudStorage = FirebaseStorage.getInstance().getReference("pfp")
                                val uniqueName = userId!!

                                cloudStorage.child(uniqueName).putFile(uri)
                                    .addOnSuccessListener {
                                        it.metadata?.reference?.downloadUrl?.addOnSuccessListener { url ->
                                            userbld.image(url.toString())
                                            val user = userbld.build()

                                            db.collection("users").document(userId).set(user)
                                                .addOnSuccessListener {
                                                    Log.d("TAG", "DocumentSnapshot added with ID: $userId")
                                                    MainActivity.DataManager.setUserData(user)
                                                    auth.signOut()
                                                    findNavController().popBackStack()
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.w("TAG", "Error adding document", e)
                                                    showToast("Ошибка при добавлении документа")
                                                }
                                        }
                                    }
                                    .addOnFailureListener {
                                        showToast("Ошибка при сохранении изображения")
                                    }
                            } else {
                                val user = userbld.build()

                                if (userId != null) {
                                    db.collection("users").document(userId).set(user)
                                        .addOnSuccessListener {
                                            Log.d("TAG", "DocumentSnapshot added with ID: $userId")
                                            MainActivity.DataManager.setUserData(user)
                                            auth.signOut()
                                            findNavController().popBackStack()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("TAG", "Error adding document", e)
                                            showToast("Ошибка при добавлении документа")
                                        }
                                }
                            }

                            
                        } else {
                            showToast("Ошибка регистрации: ${task.exception?.message}")
                        }
                    }
            }
        }

        return root
    }

    private fun areFieldsValid(): Boolean {
        val username = binding.textName.text.toString()
        val usersurname = binding.textSurname.text.toString()
        val login = binding.textLogin.text.toString()
        val password = binding.textPass.text.toString()
        val date = binding.textdate.text.toString()

        return when {
            username.isEmpty() -> {
                showToast("Имя не должно быть пустым")
                false
            }

            usersurname.isEmpty() -> {
                showToast("Фамилия не должна быть пустой")
                false
            }

            login.isEmpty() -> {
                showToast("Логин не должен быть пустым")
                false
            }

            password.isEmpty() -> {
                showToast("Пароль не должен быть пустым")
                false
            }

            date.isEmpty() -> {
                showToast("Дата не должна быть пустой")
                false
            }

            else -> true
        }
    }

    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                date.set(year, month, dayOfMonth)
                updateDateEditText()
            },
            date.get(Calendar.YEAR),
            date.get(Calendar.MONTH),
            date.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.maxDate =
            System.currentTimeMillis() // Ограничение на дату - сегодняшний день и ранее
        datePickerDialog.show()
    }

    private fun updateDateEditText() {
        val dateFormat = android.text.format.DateFormat.getDateFormat(context)
        binding.textdate.setText(dateFormat.format(date.time))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}