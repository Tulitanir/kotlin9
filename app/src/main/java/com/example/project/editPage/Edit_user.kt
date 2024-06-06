package com.example.project.editPage

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Credentials
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.project.MainActivity
import com.example.project.R
import com.example.project.databinding.FragmentEditUserBinding
import com.example.project.dialogs.PasswordChangeDialogFragment
import com.example.project.util.PasswordChangeListener
import com.example.project.util.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Calendar


class Edit_user : Fragment() {
    private var _binding: FragmentEditUserBinding? = null
    private var date = Calendar.getInstance()
    private lateinit var imagebytes: ByteArray
    private val binding get() = _binding!!
    private lateinit var pass: String
    private lateinit var uri: Uri
    private lateinit var auth: FirebaseAuth
    private var imagechanged = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUserBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val user: User? = MainActivity.DataManager.getUserData()
        auth = FirebaseAuth.getInstance()
        binding.editNameSave.setText(user!!.username)
        binding.editSurnameSave.setText(user.usersurname)
        binding.editDateSave.setText(user.date)
        if (user.image != null) {
            Picasso.get().load(user.image).into(binding.imageView)
        } else {
            binding.imageView.setImageResource(R.mipmap.ic_launcher)
        }

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                uri = it
                binding.imageView.setImageURI(uri)
                imagechanged = true
            }
        }

        binding.changeimagebutton.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.passdiagbutton.setOnClickListener {
            val dialogFragment = PasswordChangeDialogFragment()
            dialogFragment.setPasswordChangeListener(object :
                PasswordChangeListener {
                override fun onChangePassword(oldPassword: String, newPassword: String) {
                    val credentials = EmailAuthProvider.getCredential(user.email!!, oldPassword)
                    auth.currentUser?.reauthenticate(credentials)
                        ?.addOnSuccessListener {
                            pass = newPassword
                        }
                        ?.addOnFailureListener {
                            Toast.makeText(context, "Вы ввели неверный пароль", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            })
            dialogFragment.show(requireActivity().supportFragmentManager, "PasswordChangeDialog")
        }
        binding.editDateSave.setOnClickListener {
            showDatePickerDialog()
        }
        binding.saveChanges.setOnClickListener {
            var hasChanged = false
            if (binding.editNameSave.text.toString() != user.username) {
                hasChanged = true
                user.username = binding.editNameSave.text.toString()
            }
            if (binding.editSurnameSave.text.toString() != user.usersurname) {
                hasChanged = true
                user.usersurname = binding.editSurnameSave.text.toString()
            }
            if (binding.editDateSave.text.toString() != user.date) {
                hasChanged = true
                user.date = binding.editDateSave.text.toString()
            }
            if (this::pass.isInitialized) {
                hasChanged = true
                auth.currentUser?.updatePassword(pass)
            }
            if (imagechanged) {
                hasChanged = true

                val cloudStorage = FirebaseStorage.getInstance().getReference("pfp")
                val uniqueName = MainActivity.DataManager.getId()!!

                cloudStorage.child(uniqueName).putFile(uri)
                    .addOnSuccessListener {
                        it.metadata?.reference?.downloadUrl?.addOnSuccessListener { url ->
                            user.image = url.toString()
                            MainActivity.DataManager.setUserData(user)
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Ошибка при сохранении изображения", Toast.LENGTH_SHORT)
                            .show()
                    }
            }
            if (hasChanged) {
                val db = Firebase.firestore
                val id = MainActivity.DataManager.getId()
                Log.d("ad", "onCreateView: $id ")
                if (id != null) {
                    db.collection("users").document(id).set(user)
                    MainActivity.DataManager.setUserData(user)

                    val navView: NavigationView? = activity?.findViewById(R.id.nav_view)
                    navView?.menu?.performIdentifierAction(R.id.nav_slideshow, 0)
                }
            }

        }
        return root
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
        binding.editDateSave.setText(dateFormat.format(date.time))
    }
}