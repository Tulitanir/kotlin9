package com.example.project.image

import com.google.firebase.Timestamp

class ImagePost(
    var userId: String? = null,
    var description: String? = null,
    var imageUrl: String? = null,
    var date: Timestamp? = null
) {
    data class Builder(
        private var userId: String? = null,
        private var description: String? = null,
        private var imageUrl: String? = null,
        private var date: Timestamp? = null
    ) {
        fun userId(userId: String) = apply { this.userId = userId }
        fun description(description: String) = apply { this.description = description }
        fun imageUrl(imageUrl: String) = apply { this.imageUrl = imageUrl }
        fun date(date: Timestamp) = apply { this.date = date }


        fun build() = ImagePost(userId = userId, description = description, imageUrl = imageUrl, date = date)

        override fun toString(): String {
            return "Builder(userId=$userId, description=$description, imageUrl=$imageUrl, date=$date)"
        }
    }
}