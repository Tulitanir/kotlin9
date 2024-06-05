package com.example.project.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.project.R
import com.example.project.image.PostInfo
import com.squareup.picasso.Picasso

class ShowPostFragment(
    private var postInfo: PostInfo,
) : DialogFragment() {

    private lateinit var text: TextView
    private lateinit var image: ImageView
    private lateinit var button: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_show_post, container, false)
        text = view.findViewById(R.id.imageDescriptionView)
        text.text = postInfo.post.description
        image = view.findViewById(R.id.uploadedImage)
        Picasso.get().load(postInfo.post.zimageUrl).into(image)
        button = view.findViewById(R.id.exitButton)
        button.setOnClickListener {
            dismiss()
        }
        return view
    }
}