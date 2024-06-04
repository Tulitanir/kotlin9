package com.example.project.image

import com.google.firebase.firestore.Blob

class PostInfo(
    val postId: String,
    val post: ImagePost,
    val userLogin: String?,
    val userPfp: Blob?
)