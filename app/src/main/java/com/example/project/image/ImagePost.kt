package com.example.project.image

class ImagePost(
    var userId: String? = null,
    var description: String? = null,
    var zimageUrl: String? = null

) {
    data class Builder(
        private var userId: String? = null,
        private var description: String? = null,
        private var zimageUrl: String? = null
    ) {
        fun userId(userId: String) = apply { this.userId = userId }
        fun description(description: String) = apply { this.description = description }

        fun imageUrl(imageUrl: String) = apply { this.zimageUrl = imageUrl }

        fun build() = ImagePost(userId = userId, description = description, zimageUrl = zimageUrl)
        override fun toString(): String {
            return "Builder(userId=$userId, description=$description, imageUrl=$zimageUrl)"
        }
    }
}