package com.example.project.image

import com.google.firebase.firestore.Blob

class PostInfo(
    val post: ImagePost,
    val userLogin: String?,
    val userPfp: Blob?
)