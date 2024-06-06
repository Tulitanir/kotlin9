package com.example.project.comment

import com.google.firebase.Timestamp

data class Comment(
    var userId: String? = null,
    var text: String? = null,
    val date: Timestamp
) {
    constructor() : this("", "", com.google.firebase.Timestamp.now())
}
