package com.example.project.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.drawToBitmap
import androidx.fragment.app.DialogFragment
import com.example.project.R
import com.example.project.image.PostInfo
import com.example.project.util.ImageTagger
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdatePostFragment(
    private var postInfo: PostInfo,
    private val onPostEdited: (PostInfo) -> Unit
) : DialogFragment() {

    private lateinit var text: EditText
    private lateinit var image: ImageView
    private lateinit var button: Button
    private lateinit var generateButton: Button
    private lateinit var imageTagger: ImageTagger

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_update_post, container, false)
        imageTagger = ImageTagger(activity?.filesDir!!)

        text = view.findViewById(R.id.imageDescriptionEdit)
        text.setText(postInfo.post.description)
        image = view.findViewById(R.id.uploadedImage)
        Picasso.get().load(postInfo.post.imageUrl).into(image)
        button = view.findViewById(R.id.uploadChangesButton)
        button.setOnClickListener {
            postInfo.post.description = text.text.toString().trim()
            onPostEdited(postInfo)
            dismiss()
        }
        generateButton = view.findViewById(R.id.generateButton)
        generateButton.setOnClickListener {
            val bitmap = image.drawToBitmap()

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    text.setText(imageTagger.inference(bitmap))
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        e.localizedMessage?.let { context?.let {
                            Toast.makeText(it, e.localizedMessage, Toast.LENGTH_LONG).show()
                        } }
                    }
                }
            }
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }
}