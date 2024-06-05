package com.example.project.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.example.project.R
import com.example.project.image.PostInfo
import com.squareup.picasso.Picasso

class UpdatePostFragment(
    private var postInfo: PostInfo,
    private val onPostEdited: (PostInfo) -> Unit
) : DialogFragment() {

    private lateinit var text: EditText
    private lateinit var image: ImageView
    private lateinit var button: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_update_post, container, false)
        text = view.findViewById(R.id.imageDescriptionEdit)
        text.setText(postInfo.post.description)
        image = view.findViewById(R.id.uploadedImage)
        Picasso.get().load(postInfo.post.zimageUrl).into(image)
        button = view.findViewById(R.id.uploadChangesButton)
        button.setOnClickListener {
            postInfo.post.description = text.text.toString().trim()
            onPostEdited(postInfo)
            dismiss()
        }
        return view
    }
}