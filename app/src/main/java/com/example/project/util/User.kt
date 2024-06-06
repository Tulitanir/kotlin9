package com.example.project.util

import com.google.firebase.firestore.Blob

class User(
    var username: String? = null,
    var usersurname: String? = null,
    var login: String? = null,
    var email: String? = null,
    var image: String? = null,
    var date: String? = null
) {
    data class Builder(
        private var username: String? = null,
        private var usersurname: String? = null,
        private var login: String? = null,
        private var email: String? = null,
        private var password: String? = null,
        private var image: String? = null,
        private var date: String? = null
    ) {

        fun username(username: String) = apply { this.username = username }
        fun usersurname(usersurname: String) = apply { this.usersurname = usersurname }
        fun login(login: String) = apply { this.login = login }
        fun email(email: String) = apply { this.email = email }
        fun date(date: String) = apply { this.date = date }
        fun image(image: String) = apply { this.image = image }
        fun build() = User(username, usersurname, login, email, image, date)
    }

    override fun toString(): String {
        return "User(username=$username, usersurname=$usersurname, login=$login, image=$image, date=$date)"
    }

}