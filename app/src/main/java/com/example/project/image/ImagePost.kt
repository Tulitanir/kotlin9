package com.example.project.image

import com.example.project.util.User
import com.google.firebase.firestore.Blob

class ImagePost (
    var userId: String? = null,
    var description: String? = null,
    var imageUrl: String? = null
) {
    data class Builder(
        private var userId: String? = null,
        private var description: String? = null,
        private var imageUrl: String? = null
    ) {
        fun userId(userId: String) = apply { this.userId = userId }
        fun description(description: String) = apply { this.description = description }

        fun imageUrl(imageUrl: String) = apply { this.imageUrl = imageUrl }

        fun build() = ImagePost(userId = userId, description = description, imageUrl = imageUrl)
        override fun toString(): String {
            return "Builder(userId=$userId, description=$description, imageUrl=$imageUrl)"
        }
    }
}