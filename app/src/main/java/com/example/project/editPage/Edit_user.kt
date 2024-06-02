package com.example.project.editPage

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
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
import com.example.project.MainActivity
import com.example.project.R
import com.example.project.databinding.FragmentEditUserBinding
import com.example.project.dialogs.PasswordChangeDialogFragment
import com.example.project.util.PasswordChangeListener
import com.example.project.util.User
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.firestore
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
    private var imagechanged = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUserBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val user: User? = MainActivity.DataManager.getUserData()
        binding.editNameSave.setText(user!!.username)
        binding.editSurnameSave.setText(user.usersurname)
        binding.editDateSave.setText(user.date)
        if (user.image != null) {
            val imagebytesdb = user.image!!.toBytes()
            binding.imageView.setImageBitmap(
                BitmapFactory.decodeByteArray(
                    imagebytesdb,
                    0,
                    imagebytesdb.size
                )
            )
        } else {
            binding.imageView.setImageResource(R.mipmap.ic_launcher)
        }
        binding.changeimagebutton.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start()
        }
        binding.passdiagbutton.setOnClickListener {
            val dialogFragment = PasswordChangeDialogFragment()
            dialogFragment.setPasswordChangeListener(object :
                PasswordChangeListener {
                override fun onChangePassword(oldPassword: String, newPassword: String) {
                    if (user.password == oldPassword) {
                        pass = newPassword
                        Log.d("adg", pass)
                    } else {
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
            if (this::pass.isInitialized && pass != user.password) {
                hasChanged = true
                user.password = pass
            }
            if (imagechanged) {
                hasChanged = true
                val bitmap: Bitmap = (binding.imageView.drawable as BitmapDrawable).bitmap
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
                user.image = Blob.fromBytes(byteArrayOutputStream.toByteArray())
            }
            if (hasChanged) {
                val db = Firebase.firestore
                val id = MainActivity.DataManager.getId()
                Log.d("ad", "onCreateView: $id ")
                if (id != null) {
                    db.collection("users").document(id).set(user)
                    MainActivity.DataManager.setUserData(user)
                    updateUserInfo(user)

                    val navView: NavigationView? = activity?.findViewById(R.id.nav_view)
                    navView?.menu?.performIdentifierAction(R.id.nav_slideshow, 0)
                }
            }

        }
        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            uri = data?.data!!
            val imageFile = File(uri.path!!)
            val imageStream: InputStream = FileInputStream(imageFile)
            imagebytes = imageStream.readBytes()
            val user = MainActivity.DataManager.getUserData()
            if (!imagechanged && user != null) {
                if (user.image == null || !imagebytes.contentEquals(user.image!!.toBytes()))
                    imagechanged = true
            }
            binding.imageView.setImageBitmap(
                BitmapFactory.decodeByteArray(
                    imagebytes,
                    0,
                    imagebytes.size
                )
            )

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
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
        binding.editDateSave.setText(dateFormat.format(date.time))
    }

    private fun updateUserInfo(user: User) {
        val header =
            (requireActivity() as AppCompatActivity).findViewById<NavigationView>(R.id.nav_view)
                ?.getHeaderView(0)

        var text = header?.findViewById<TextView>(R.id.headertextNameSurname)
        text!!.text = buildString {
            append(user.username)
            append(" ")
            append(user.usersurname)
        }
        text = header?.findViewById(R.id.headertextLogin)
        text!!.text = buildString {
            append(user.login)
        }
        text = header?.findViewById(R.id.headertextDate)
        text!!.text = buildString {
            append(user.date)
        }
        if (user.image != null) {
            val blb: Blob = user.image as Blob
            val arr = blb.toBytes()
            val bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.size)
            val image = header?.findViewById<ImageView>(R.id.headerimageView)
            image?.setImageBitmap(bitmap)

        }
    }
}