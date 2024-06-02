package com.example.project.registation

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
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.project.databinding.FragmentRegistationBinding
import com.example.project.util.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Calendar


class RegistationFragment : Fragment() {

    private lateinit var imagebytes: ByteArray
    private lateinit var uri: Uri
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private var date = Calendar.getInstance()
    private var _binding: FragmentRegistationBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistationBinding.inflate(inflater, container, false)
        val root: View = binding.root
        binding.button2.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }

        binding.textdate.setOnClickListener {
            showDatePickerDialog()
        }
        binding.button3.setOnClickListener {
            if (areFieldsValid()) {
                val db = Firebase.firestore
                FirebaseFirestore.setLoggingEnabled(true)

                val userbld = User.Builder()
                    .username(binding.textName.text.toString())
                    .usersurname(binding.textSurname.text.toString())
                    .login(binding.textLogin.text.toString())
                    .password(binding.textPass.text.toString())
                    .date(binding.textdate.text.toString())

                if (this::uri.isInitialized) {
                    userbld.image(imagebytes)
                }

                val user = userbld.build()

                // Проверяем, существует ли пользователь с таким логином
                db.collection("users")
                    .whereEqualTo("login", binding.textLogin.text.toString())
                    .get()
                    .addOnSuccessListener { documents ->
                        if (documents.isEmpty) {
                            // Логин не занят, добавляем нового пользователя
                            db.collection("users")
                                .add(user)
                                .addOnSuccessListener { documentReference ->
                                    Log.d(
                                        "TAG",
                                        "DocumentSnapshot added with ID: ${documentReference.id}"
                                    )
                                    findNavController().popBackStack() // Возвращаемся назад только после успешного добавления
                                }
                                .addOnFailureListener { e ->
                                    Log.w("TAG", "Error adding document", e)
                                    showToast("Ошибка при добавлении документа")
                                }
                        } else {
                            // Логин уже занят, уведомляем пользователя
                            showToast("Логин уже занят. Пожалуйста, выберите другой.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.w("TAG", "Error checking document", e)
                        showToast("Ошибка при проверке документа")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            uri = data?.data!!
            val imageFile = File(uri.path!!)
            val imageStream: InputStream = FileInputStream(imageFile)
            imagebytes = imageStream.readBytes()
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}