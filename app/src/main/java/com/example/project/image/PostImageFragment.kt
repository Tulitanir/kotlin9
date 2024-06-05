package com.example.project.image

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.project.MainActivity
import com.example.project.R
import com.google.android.material.navigation.NavigationView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class PostImageFragment : Fragment() {
    private lateinit var imageView: ImageView
    private lateinit var editText: EditText
    private lateinit var selectButton: Button
    private lateinit var uploadButton: Button
    private lateinit var cloudStorage: StorageReference

    private var uri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post_image, container, false)

        imageView = view.findViewById(R.id.uploadedImage)
        editText = view.findViewById(R.id.imageDescription)
        selectButton = view.findViewById(R.id.selectButton)
        uploadButton = view.findViewById(R.id.uploadButton)

        val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) {
                imageView.setImageURI(it)
                imageView.visibility = View.VISIBLE
                editText.visibility = View.VISIBLE
                uri = it
            }
        }

        selectButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        uploadButton.setOnClickListener {
            uploadPost()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cloudStorage = FirebaseStorage.getInstance().getReference("images")
    }

    private fun uploadPost() {
        val description = editText.text.toString().trim()

        if (checkData(description)) {
            uri?.let {
                val uniqueName = UUID.randomUUID().toString() + "_" + (it.lastPathSegment
                    ?: "default_image_name")
                cloudStorage.child(uniqueName).putFile(it)
                    .addOnSuccessListener { taskSnapshot ->
                        taskSnapshot.metadata?.reference?.downloadUrl
                            ?.addOnSuccessListener { url ->
                                val userId = MainActivity.DataManager.getId()
                                val db = Firebase.firestore
                                FirebaseFirestore.setLoggingEnabled(true)

                                userId?.let {
                                    val post = ImagePost.Builder()
                                        .imageUrl(url.toString())
                                        .description(description)
                                        .userId(userId)
                                        .build()

                                    db.collection("posts").add(post)
                                        .addOnSuccessListener {
                                            val navView: NavigationView? =
                                                activity?.findViewById(R.id.nav_view)
                                            navView?.menu?.performIdentifierAction(
                                                R.id.nav_slideshow,
                                                0
                                            )
                                        }
                                        .addOnFailureListener {
                                            showToast("Не удалось создать пост")
                                        }
                                }
                            }
                            ?.addOnFailureListener {
                                showToast("Не удалось получить URL изображения")
                            }
                    }
                    .addOnFailureListener {
                        showToast("Не удалось загрузить изображение")
                    }
            } ?: showToast("Выберите изображение")
        }
    }

    private fun checkData(description: String): Boolean {
        if (uri == null) {
            showToast("Выберите изображение")
            return false
        }

        if (description.isEmpty()) {
            showToast("Введите описание")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
    }
}